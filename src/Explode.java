import java.awt.*;

/**************************
 * * Young Shaw * 2017��5��18�� * *
 **************************/

public class Explode {

	public static final int[] DIAMETER = { 4, 7, 12, 18, 26, 32, 49, 30, 14, 6 };

	private int explodeX, explodeY;
	private boolean live = true;
	private int index = 0;// ��ʾ��ը���е��ڼ����ˣ�����ѡ��ֱ�������еĲ�����
	private TankClient tc;

	public Explode(int explodeX, int explodeY, TankClient tc) {
		this.explodeX = explodeX;
		this.explodeY = explodeY;
		this.tc = tc;
	}

	public void explodeDraw(Graphics g) {
		if (!live) {
			tc.explodes.remove(this);
			return; // �����ڵ���˼·
		}
		if (index == DIAMETER.length) {
			live = false;
			index = 0;
			return;
		}

		Color c = g.getColor();
		g.setColor(Color.orange);
		g.fillOval(explodeX, explodeY, DIAMETER[index], DIAMETER[index]);
		index++;
		g.setColor(c);
	}
}
