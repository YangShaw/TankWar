import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Random;

/**************************
 * * Young Shaw * 2017��5��16�� * *
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
	private final static int SUPERMISSILE_MAX = 3; // �����ڵ�������
	private final static int POWER_NEED = 3; // ��ɱ���λ�ó����ڵ�����
	
	public int[] missileRate={48,45,40,35};	//	�з��ڵ������Ƶ�ʣ�Խ�ӽ�50Ƶ��Խ�͡����ܵ�����ʮ��

	// ���õз�̹���Լ�����ֻ��Ҫ��̹�˸��ϳ�ʼ�ķ���Ϳ����ˡ���Ϊ��move�����У�ֻҪdirection����stop�ͻ��ƶ���
	// ���������ֱ�ӷ�����ĳ�Ա������ֻ��ͨ������⹫���ķ���
	private int tankX, tankY; // λ��
	private int oldX, oldY; // ��һ����λ��

	private int life = TANK_MAX_BLOOD;

	private static Random r = new Random();// �����������

	private boolean live = true;// ̹���Ƿ�����

	private Direction[] dirs = Direction.values();

	private TankClient tc;

	private boolean good;

	private int step = r.nextInt(15) + 5;

	private int powerCount = 0;
	private int superMissile = 0;

	private void addSuperMissile() {
		if (powerCount >= POWER_NEED && superMissile < SUPERMISSILE_MAX) { // ����������������ڵ�
			powerCount = 0;
			superMissile++;
		}
	}

	public void addPower() { // ��ɱ���λ��һ���ڵ�
		if (powerCount < POWER_NEED) {
			powerCount++;
		}
		addSuperMissile();
	}

	private boolean bR = false, bD = false, bL = false, bU = false; // �ж��Ƿ������⼸������

	enum Direction {
		L, LU, U, RU, R, RD, D, LD, STOP
	};

	private BloodBar bb = new BloodBar();

	private Direction direction = Direction.STOP;
	private Direction barrelDir = Direction.D; // ��Ͳ����,�����ڵ�����ķ���

	// ʹ̹�˵��ƶ����ȣ�ÿ�ΰ��·������ֻ�ı�̹�˵ķ���Ȼ����draw�и��ݷ����Զ���ǰ�ߡ�

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

	

	public void tankDraw(Graphics g) { // ����һ֧���ʹ����Ϳ����ˡ�
		if (!live) {
			if (!good) {
				tc.enemyTanks.remove(this);
			}
			return; // ���û�л��ߣ��Ͳ��û���ֱ�ӡ�

		}

		Color c = g.getColor(); // ǰ��ɫ���ȱ�������
		if (good) {
			g.setColor(Color.red); // ����ǰ��ɫ
		} else {
			g.setColor(Color.blue);
		}
		g.fillOval(tankX, tankY, TANK_WIDTH, TANK_HEIGHT); // ��һ��Բ��ǰ�������������Ͻ����꣬�����������ǿ�͸ߣ�Բ��������ε�����Բ��
		g.setColor(c); // ��ԭ������ɫ�������ò���ǸĹ�֮����������ڵ���ɫ������ԲȦ�Ժ��ٰ���ɫ�Ļ�ȥ����������ǣ���Ҫ�Ҹ��˼ҵ�ǰ��ɫ

		if (this.isGood()) {
			bb.barDraw(g);
		}
		
		barrelDraw(g);
		
		if (this.direction != Direction.STOP) { // ��Ͳ����Ϊ̹�˷�����һ�����⣬����������Ϊб���ĸ�����
			this.barrelDir = this.direction;
		}

		move();
	}
	
	
	
	

	public void tankKeyPressed(KeyEvent e) { // ̹���ƶ�	������������
		int key = e.getKeyCode(); // ��ȡ���̰�ť��ӳ����
		switch (key) {

		case KeyEvent.VK_RIGHT: // �ң��£����ϰ�ť
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
	

	public void tankKeyReleased(KeyEvent e) {	//	���������ͷ�
		int key = e.getKeyCode(); // ��ȡ���̰�ť��ӳ����
		switch (key) {
		case KeyEvent.VK_CONTROL:
			fire();
			break;
		case KeyEvent.VK_RIGHT: // �ң��£����ϰ�ť
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
	
	public Rectangle getRect() {	//	��ײ���
		return new Rectangle(tankX, tankY, TANK_WIDTH, TANK_HEIGHT);
	}

	
	public boolean collidesWithWall(Wall w) {	//	��ǽ��ײ��Ч�����ص�֮ǰ��λ�ã�
		if (this.live && this.getRect().intersects(w.getRect())) {
			this.stay();
			return true;
		}
		return false;
	}

	public boolean collidesWithTanks(java.util.List<Tank> tanks) {	//	��̹����ײ��Ч�������ص�֮ǰ��λ�ã�
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

	public boolean eatBlood(Blood b) {	//	�Ե�Ѫҩ��Ч������Ѫ��
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
	private void collideWithBorder(){	//	������ײ�߽�
		if (tankX < 0)
			tankX = 0;
		if (tankY < 0 + 30)
			tankY = 0 + 30; // �������Ŀ��
		if (tankX > Constant.FRAME_X - TANK_WIDTH) // ����̹�˵Ŀ�ߡ���Ϊ��ߺ��ϱ���x��y������ο��㣬���üӡ�
			tankX = Constant.FRAME_X - TANK_WIDTH;
		if (tankY > Constant.FRAME_Y - TANK_HEIGHT)
			tankY = Constant.FRAME_Y - TANK_HEIGHT;
	}
	
	private void enemyTankMove(){	//	����з�̹�˵�����ƶ�
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
	
	private void myTankMove(){	//	��������ܿ��ƶ�
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
		this.oldX = tankX;	//	��¼�ƶ�ǰ��λ��
		this.oldY = tankY;

		myTankMove();
		enemyTankMove();
		collideWithBorder();

	}

	private void locateDirection() { // ���ݼ�������������ȷ�����򡣲��������ɼ������İ���������
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

	private Missile fire() {	//	��������Ŀ��� ������Ϊ��ǰǰ������
		if (!live) {
			return null;
		}
		int missileX = tankX + TANK_WIDTH / 2 - Missile.MISSILE_WIDTH / 2;
		int missileY = tankY + TANK_HEIGHT / 2 - Missile.MISSILE_HEIGHT / 2;
		Missile m = new Missile(missileX, missileY, good, barrelDir, tc);
		tc.missiles.add(m);
		return m;
	}

	private Missile fire(Direction dir) {	//	������Ŀ��𣬸������ڵ�ʹ��
		if (!live) {
			return null;
		}
		int missileX = tankX + TANK_WIDTH / 2 - Missile.MISSILE_WIDTH / 2;
		int missileY = tankY + TANK_HEIGHT / 2 - Missile.MISSILE_HEIGHT / 2;
		Missile m = new Missile(missileX, missileY, good, dir, tc);
		tc.missiles.add(m);
		return m;
	}

	private void superFire() {	//	�����ڵ�����Ч����˸������䣩
		Direction[] dirs = Direction.values();
		for (int i = 0; i < 8; i++) {
			fire(dirs[i]);
		}
	}
	
	private void canSuperFire(){	//	�ж��Ƿ���Է��䳬���ڵ�
		if (superMissile > 0) {
			superFire();
			superMissile--;
		}
	}

	private void stay() {
		tankX = oldX;
		tankY = oldY;
	}

	private void barrelDraw(Graphics g){	//	��������Ͳ
		switch (barrelDir) { // drawLine:����
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
	
	private void tankRestart(){	//	̹�˸�����¿�ʼ��ͬʱ���¼Ʒ֣��з�̹������
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
