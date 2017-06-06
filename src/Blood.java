import java.awt.*;

/**************************
 * * Young Shaw * 2017年5月30日 * *
 **************************/

public class Blood {

	public static final int BLOOD_ADD = 20;
	int bloodX, bloodY, bloodW, bloodH;
	TankClient tc;

	private boolean live;

	int step = 0;

	/*
	 * 设置路线，按照轨迹运动： 设置一个二维数组，按照数组里的点运动。
	 */
	private int[][] position = { { 350, 300 }, { 360, 300 }, { 375, 275 }, { 400, 200 }, { 360, 270 } };

	public Blood() {
		this.bloodX = position[0][0];
		this.bloodY = position[0][1];
		this.bloodW = 15;
		this.bloodH = 15;
		this.live=true;
	}

	public Blood(int bloodX, int bloodY, int bloodW, int bloodH, TankClient tc) {
		this.bloodX = bloodX;
		this.bloodY = bloodY;
		this.bloodW = bloodW;
		this.bloodH = bloodH;
		this.tc = tc;
		this.live=true;
	}

	private void move() {
		step++;
		if (step == position.length * 100) {
			step = 0;
		}
		this.bloodX = position[step / 100][0];
		this.bloodY = position[step / 100][1];
	}

	public void bloodDraw(Graphics g) {
		if (this.live) {
			Color c = g.getColor();
			g.setColor(Color.magenta);
			g.fillRect(bloodX, bloodY, bloodW, bloodH);
			g.setColor(c);
			move();
		}
	}

	public Rectangle getRect() {
		return new Rectangle(bloodX, bloodY, bloodW, bloodH);
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}
}
