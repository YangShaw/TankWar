import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

/**************************
 * * Young Shaw * 2017��5��16�� * *
 **************************/

public class Missile {

	// ʹ�����������ڵ���ÿ��̧��ctrl����������װ���µ��ڵ������͵�ʹ�á�

	public static final int MISSILE_X_V = 3;
	public static final int MISSILE_Y_V = 3;
	public static final int MISSILE_WIDTH = 10;
	public static final int MISSILE_HEIGHT = 10;
	public static final int MISSILE_ATTACK = 10;

	private int missileX, missileY;
	private Tank.Direction direction;
	private TankClient tc; // ������ʱɾ��Խ����ڵ���
	private boolean live = true; // ��ʾ�ڵ��Ƿ������Ĳ���ֵ��
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
		Color c = g.getColor(); // ǰ��ɫ���ȱ�������
		if (good)
			g.setColor(Color.black); // ����ǰ��ɫ
		else
			g.setColor(Color.white);
		g.fillOval(missileX, missileY, 10, 10); // ��һ��Բ��ǰ�������������Ͻ����꣬�����������ǿ�͸ߣ�Բ��������ε�����Բ��
		g.setColor(c); // ��ԭ������ɫ�������ò���ǸĹ�֮����������ڵ���ɫ������ԲȦ�Ժ��ٰ���ɫ�Ļ�ȥ����������ǣ���Ҫ�Ҹ��˼ҵ�ǰ��ɫ
		move();

	}

	private void move() {
		if (!live) { // �ƶ�֮ǰ�ж�һ���ڵ��Ƿ���š�
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
		// move��ÿ��λʱ����еĶ����������̵߳Ĳ���ˢ�£�50ms����move���ϱ�����
		// ����ÿһ��move֮��Ҫ�ж�һ���ڵ��Ƿ����
		if (missileX < 0 || missileX > Constant.FRAME_X || missileY < 0 || missileY > Constant.FRAME_Y) {
			live = false;
			// tc.missiles.remove(this);
		}
	}

	public Rectangle getRect() {
		return new Rectangle(missileX, missileY, MISSILE_WIDTH, MISSILE_HEIGHT);
	}

	public boolean hitTank(Tank tank) { // �ж��Ƿ����
		// ��ײ���
		// intersects�����������rectangle�Ƿ��ཻ��
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
