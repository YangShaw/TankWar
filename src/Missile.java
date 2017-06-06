import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

/**************************
 * * Young Shaw * 2017年5月16日 * *
 **************************/

public class Missile {

	// 使用容器保存炮弹。每当抬起ctrl就往容器中装入新的炮弹。泛型的使用。

	public static final int MISSILE_X_V = 3;
	public static final int MISSILE_Y_V = 3;
	public static final int MISSILE_WIDTH = 10;
	public static final int MISSILE_HEIGHT = 10;
	public static final int MISSILE_ATTACK = 10;

	private int missileX, missileY;
	private Tank.Direction direction;
	private TankClient tc; // 用于随时删除越界的炮弹。
	private boolean live = true; // 表示炮弹是否消亡的布尔值。
	private boolean good;

	public boolean isLive() {
		return live;
	}

	public Missile(int missileX, int missileY, Tank.Direction direction) {
		this.missileX = missileX;
		this.missileY = missileY;
		this.direction = direction;
	}

	public Missile(int missileX, int missileY, boolean good, Tank.Direction direction, TankClient tc) {
		this(missileX, missileY, direction);
		this.good = good;
		this.tc = tc;
	}

	public Missile(int missileX, int missileY) {
		this.missileX = missileX;
		this.missileY = missileY;
	}

	public void missileDraw(Graphics g) {
		Color c = g.getColor(); // 前景色，先报存下来
		if (good)
			g.setColor(Color.black); // 设置前景色
		else
			g.setColor(Color.white);
		g.fillOval(missileX, missileY, 10, 10); // 画一个圆，前两个参数是左上角坐标，后两个参数是宽和高，圆是这个矩形的内切圆。
		g.setColor(c); // 把原来的颜色设回来，貌似是改过之后绘制用现在的颜色，画完圆圈以后再把颜色改回去。这告诉我们，不要乱改人家的前景色
		move();

	}

	private void move() {
		if (!live) { // 移动之前判断一下炮弹是否活着。
			tc.missiles.remove(this);
		}

		switch (direction) {
		case L:
			missileX -= MISSILE_X_V;
			break;
		case LU:
			missileX -= MISSILE_X_V;
			missileY -= MISSILE_Y_V;
			break;
		case U:
			missileY -= MISSILE_Y_V;
			break;
		case RU:
			missileX += MISSILE_X_V;
			missileY -= MISSILE_Y_V;
			break;
		case R:
			missileX += MISSILE_X_V;
			break;
		case RD:
			missileX += MISSILE_X_V;
			missileY += MISSILE_Y_V;
			break;
		case D:
			missileY += MISSILE_Y_V;
			break;
		case LD:
			missileX -= MISSILE_X_V;
			missileY += MISSILE_Y_V;
			break;
		default:
			break;

		}
		// move是每单位时间进行的动作。随着线程的不断刷新（50ms），move不断被调用
		// 所以每一次move之后要判断一下炮弹是否出界
		if (missileX < 0 || missileX > Constant.FRAME_X || missileY < 0 || missileY > Constant.FRAME_Y) {
			live = false;
			// tc.missiles.remove(this);
		}
	}

	public Rectangle getRect() {
		return new Rectangle(missileX, missileY, MISSILE_WIDTH, MISSILE_HEIGHT);
	}

	public boolean hitTank(Tank tank) { // 判断是否打到了
		// 碰撞检测
		// intersects方法检测两个rectangle是否相交。
		if (this.live && this.getRect().intersects(tank.getRect()) && tank.isLive() && this.good != tank.isGood()) {
			if (tank.isGood()) {
				tank.setLife(tank.getLife() - MISSILE_ATTACK);
				if (tank.getLife() <= 0) {
					tank.setLive(false);
				}
			}

			else {
				tc.myTank.addPower();
				tank.setLive(false);
			}
			this.live = false;
			Explode e = new Explode(missileX, missileY, tc);
			tc.explodes.add(e);
			return true;
		}
		return false;
	}

	public boolean hitTanks(List<Tank> tanks) {
		for (int i = 0; i < tanks.size(); i++) {
			if (hitTank(tanks.get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean hitWall(Wall w) {
		if (this.live && this.getRect().intersects(w.getRect())) {
			this.live = false;
			return true;
		}
		return false;
	}
}
