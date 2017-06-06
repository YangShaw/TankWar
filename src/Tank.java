import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Random;

/**************************
 * * Young Shaw * 2017年5月16日 * *
 **************************/

public class Tank {

	/*
	 * parameters about tank.
	 * 
	 */
	public final static int TANK_X_V = 5;
	public final static int TANK_Y_V = 5;
	public final static int TANK_WIDTH = 30;
	public final static int TANK_HEIGHT = 30;
	public final static int TANK_MAX_BLOOD = 100;
	private final static int SUPERMISSILE_MAX = 3; // 超级炮弹持有量
	private final static int POWER_NEED = 3; // 击杀几次获得超级炮弹充能
	
	public int[] missileRate={48,45,40,35};	//	敌方炮弹开火的频率，越接近50频率越低。不能等于五十。

	// 想让敌方坦克自己动，只需要给坦克赋上初始的方向就可以了。因为在move方法中，只要direction不是stop就会移动。
	// 不允许别人直接访问你的成员变量，只能通过你对外公开的方法
	private int tankX, tankY; // 位置
	private int oldX, oldY; // 上一步的位置

	private int life = TANK_MAX_BLOOD;

	private static Random r = new Random();// 随机数产生器

	private boolean live = true;// 坦克是否消亡

	private Direction[] dirs = Direction.values();

	private TankClient tc;

	private boolean good;

	private int step = r.nextInt(15) + 5;

	private int powerCount = 0;
	private int superMissile = 0;

	private void addSuperMissile() {
		if (powerCount >= POWER_NEED && superMissile < SUPERMISSILE_MAX) { // 至多持有三发超级炮弹
			powerCount = 0;
			superMissile++;
		}
	}

	public void addPower() { // 击杀三次获得一发炮弹
		if (powerCount < POWER_NEED) {
			powerCount++;
		}
		addSuperMissile();
	}

	private boolean bR = false, bD = false, bL = false, bU = false; // 判断是否按下了这几个键。

	enum Direction {
		L, LU, U, RU, R, RD, D, LD, STOP
	};

	private BloodBar bb = new BloodBar();

	private Direction direction = Direction.STOP;
	private Direction barrelDir = Direction.D; // 炮筒方向,决定炮弹发射的方向

	// 使坦克的移动均匀：每次按下方向键，只改变坦克的方向，然后在draw中根据方向自动朝前走。

	public Tank(int tankX, int tankY, boolean good) {
		this.tankX = tankX;
		this.tankY = tankY;
		this.oldX = tankX;
		this.oldY = tankY;
		this.good = good;
	}

	public Tank(int tankX, int tankY, boolean good, Direction direction, TankClient tc) {
		this(tankX, tankY, good);
		this.direction = direction;
		this.tc = tc;
	}

	private class BloodBar {
		public void barDraw(Graphics g) {
			Color c = g.getColor();
			g.setColor(Color.red);
			g.drawRect(tankX, tankY - 10, TANK_WIDTH, 10);
			int w = TANK_WIDTH * life / 100;
			g.fillRect(tankX, tankY - 10, w, 10);
			g.setColor(c);
		}
	}

	

	public void tankDraw(Graphics g) { // 传递一支画笔过来就可以了。
		if (!live) {
			if (!good) {
				tc.enemyTanks.remove(this);
			}
			return; // 如果没有或者，就不用画了直接。

		}

		Color c = g.getColor(); // 前景色，先报存下来
		if (good) {
			g.setColor(Color.red); // 设置前景色
		} else {
			g.setColor(Color.blue);
		}
		g.fillOval(tankX, tankY, TANK_WIDTH, TANK_HEIGHT); // 画一个圆，前两个参数是左上角坐标，后两个参数是宽和高，圆是这个矩形的内切圆。
		g.setColor(c); // 把原来的颜色设回来，貌似是改过之后绘制用现在的颜色，画完圆圈以后再把颜色改回去。这告诉我们，不要乱改人家的前景色

		if (this.isGood()) {
			bb.barDraw(g);
		}
		
		barrelDraw(g);
		
		if (this.direction != Direction.STOP) { // 炮筒方向即为坦克方向。有一个问题，这样不可能为斜的四个方向。
			this.barrelDir = this.direction;
		}

		move();
	}
	
	
	
	

	public void tankKeyPressed(KeyEvent e) { // 坦克移动	监听按键按下
		int key = e.getKeyCode(); // 获取键盘按钮的映射码
		switch (key) {

		case KeyEvent.VK_RIGHT: // 右，下，左，上按钮
			bR = true;
			break;
		case KeyEvent.VK_DOWN:
			bD = true;
			break;
		case KeyEvent.VK_LEFT:
			bL = true;
			break;
		case KeyEvent.VK_UP:
			bU = true;
			break;
		case KeyEvent.VK_A:
			canSuperFire();
			break;
		case KeyEvent.VK_F2:
			tankRestart();
			break;
		default:
			break;
		}

		locateDirection();
	}
	

	public void tankKeyReleased(KeyEvent e) {	//	监听按键释放
		int key = e.getKeyCode(); // 获取键盘按钮的映射码
		switch (key) {
		case KeyEvent.VK_CONTROL:
			fire();
			break;
		case KeyEvent.VK_RIGHT: // 右，下，左，上按钮
			bR = false;
			break;
		case KeyEvent.VK_DOWN:
			bD = false;
			break;
		case KeyEvent.VK_LEFT:
			bL = false;
			break;
		case KeyEvent.VK_UP:
			bU = false;
			break;
		}
		locateDirection();
	}
	
	public Rectangle getRect() {	//	碰撞检测
		return new Rectangle(tankX, tankY, TANK_WIDTH, TANK_HEIGHT);
	}

	
	public boolean collidesWithWall(Wall w) {	//	与墙碰撞的效果（回到之前的位置）
		if (this.live && this.getRect().intersects(w.getRect())) {
			this.stay();
			return true;
		}
		return false;
	}

	public boolean collidesWithTanks(java.util.List<Tank> tanks) {	//	与坦克碰撞的效果（都回到之前的位置）
		for (int i = 0; i < tanks.size(); i++) {
			Tank t = tanks.get(i);
			if (this != t && this.live && this.getRect().intersects(t.getRect())) {
				this.stay();
				t.stay();
				return true;
			}
		}
		return false;
	}

	public boolean eatBlood(Blood b) {	//	吃到血药的效果，加血，
		if (this.isGood() && this.live && b.isLive() && this.getRect().intersects(b.getRect())) {
			this.life = this.life + Blood.BLOOD_ADD;
			if (this.life > TANK_MAX_BLOOD) {
				this.life = TANK_MAX_BLOOD;
			}
			b.setLive(false);
			return true;
		}
		return false;
	}
	private void collideWithBorder(){	//	处理碰撞边界
		if (tankX < 0)
			tankX = 0;
		if (tankY < 0 + 30)
			tankY = 0 + 30; // 标题栏的宽度
		if (tankX > Constant.FRAME_X - TANK_WIDTH) // 加上坦克的宽高。因为左边和上边是x，y的坐标参考点，不用加。
			tankX = Constant.FRAME_X - TANK_WIDTH;
		if (tankY > Constant.FRAME_Y - TANK_HEIGHT)
			tankY = Constant.FRAME_Y - TANK_HEIGHT;
	}
	
	private void enemyTankMove(){	//	处理敌方坦克的随机移动
		if (!good) {
			if (step == 0) {
				step = r.nextInt(15) + 5;
				int rn = r.nextInt(dirs.length);
				direction = dirs[rn];
			}
			step--;
			if (r.nextInt(50) > 40)
				this.fire();
		}
	}
	
	private void myTankMove(){	//	处理本身的受控移动
		switch (direction) {
		case L:
			tankX -= TANK_X_V;
			break;
		case LU:
			tankX -= TANK_X_V;
			tankY -= TANK_Y_V;
			break;
		case U:
			tankY -= TANK_Y_V;
			break;
		case RU:
			tankX += TANK_X_V;
			tankY -= TANK_Y_V;
			break;
		case R:
			tankX += TANK_X_V;
			break;
		case RD:
			tankX += TANK_X_V;
			tankY += TANK_Y_V;
			break;
		case D:
			tankY += TANK_Y_V;
			break;
		case LD:
			tankX -= TANK_X_V;
			tankY += TANK_Y_V;
			break;
		case STOP:
			break;
		}
	}

	private void move() {
		this.oldX = tankX;	//	记录移动前的位置
		this.oldY = tankY;

		myTankMove();
		enemyTankMove();
		collideWithBorder();

	}

	private void locateDirection() { // 根据几个布尔变量来确定方向。布尔变量由监听到的按键决定。
		if (bL && !bU && !bR && !bD)
			direction = Direction.L;
		else if (bL && bU && !bR && !bD)
			direction = Direction.LU;
		else if (!bL && bU && !bR && !bD)
			direction = Direction.U;
		else if (!bL && bU && bR && !bD)
			direction = Direction.RU;
		else if (!bL && !bU && bR && !bD)
			direction = Direction.R;
		else if (!bL && !bU && bR && bD)
			direction = Direction.RD;
		else if (!bL && !bU && !bR && bD)
			direction = Direction.D;
		else if (bL && !bU && !bR && bD)
			direction = Direction.LD;
		else if (!bL && !bU && !bR && !bD)
			direction = Direction.STOP;

	}

	private Missile fire() {	//	不带方向的开火， 开火方向为当前前进方向。
		if (!live) {
			return null;
		}
		int missileX = tankX + TANK_WIDTH / 2 - Missile.MISSILE_WIDTH / 2;
		int missileY = tankY + TANK_HEIGHT / 2 - Missile.MISSILE_HEIGHT / 2;
		Missile m = new Missile(missileX, missileY, good, barrelDir, tc);
		tc.missiles.add(m);
		return m;
	}

	private Missile fire(Direction dir) {	//	带方向的开火，给超级炮弹使用
		if (!live) {
			return null;
		}
		int missileX = tankX + TANK_WIDTH / 2 - Missile.MISSILE_WIDTH / 2;
		int missileY = tankY + TANK_HEIGHT / 2 - Missile.MISSILE_HEIGHT / 2;
		Missile m = new Missile(missileX, missileY, good, dir, tc);
		tc.missiles.add(m);
		return m;
	}

	private void superFire() {	//	超级炮弹的特效（向八个方向发射）
		Direction[] dirs = Direction.values();
		for (int i = 0; i < 8; i++) {
			fire(dirs[i]);
		}
	}
	
	private void canSuperFire(){	//	判断是否可以发射超级炮弹
		if (superMissile > 0) {
			superFire();
			superMissile--;
		}
	}

	private void stay() {
		tankX = oldX;
		tankY = oldY;
	}

	private void barrelDraw(Graphics g){	//	用来画炮筒
		switch (barrelDir) { // drawLine:画线
		case L:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2,
					(int) (tankX + TANK_WIDTH / 2 - TANK_WIDTH / 2 * Math.sqrt(2)), tankY + TANK_HEIGHT / 2);
			break;
		case LU:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX, tankY);
			break;
		case U:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX + TANK_WIDTH / 2,
					(int) (tankY + TANK_HEIGHT / 2 - TANK_HEIGHT / 2 * Math.sqrt(2)));
			break;
		case RU:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX + TANK_WIDTH, tankY);
			break;
		case R:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2,
					(int) (tankX + TANK_WIDTH / 2 + TANK_WIDTH / 2 * Math.sqrt(2)), tankY + TANK_HEIGHT / 2);
			break;
		case RD:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX + TANK_WIDTH, tankY + TANK_HEIGHT);
			break;
		case D:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX + TANK_WIDTH / 2,
					(int) (tankY + TANK_HEIGHT / 2 + TANK_HEIGHT / 2 * Math.sqrt(2)));
			break;
		case LD:
			g.drawLine(tankX + TANK_WIDTH / 2, tankY + TANK_HEIGHT / 2, tankX, tankY + TANK_HEIGHT);
			break;
		case STOP:
			break;
		}
	}
	
	private void tankRestart(){	//	坦克复活，重新开始，同时重新计分，敌方坦克重置
		if (!tc.myTank.live) {
			tc.myTank = new Tank(TankClient.START_X, TankClient.START_Y, true, Tank.Direction.STOP, tc);
			tc.setScore(0);
			tc.enemyRestart();
		}
	}
	

	
	public int getTankX() {
		return tankX;
	}

	public void setTankX(int tankX) {
		this.tankX = tankX;
	}

	public int getTankY() {
		return tankY;
	}

	public void setTankY(int tankY) {
		this.tankY = tankY;
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

	public boolean isGood() {
		return good;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public int getPowerCount() {
		return powerCount;
	}

	public void setPowerCount(int powerCount) {
		this.powerCount = powerCount;
	}

	public int getSuperMissile() {
		return superMissile;
	}

	public void setSuperMissile(int superMissile) {
		this.superMissile = superMissile;
	}

}
