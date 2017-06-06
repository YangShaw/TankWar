
/**************************
 *                        *
 * Young Shaw             *
 * 2017��5��30��                	  *
 *                        *
 **************************/
import java.awt.*;

public class Wall {
	int wallX, wallY, wallW, wallH;
	TankClient tc;

	public Wall(int wallX, int wallY, int wallW, int wallH, TankClient tc) {
		this.wallX = wallX;
		this.wallY = wallY;
		this.wallW = wallW;
		this.wallH = wallH;
		this.tc = tc;
	}

	public void wallDraw(Graphics g) {
		g.fillRect(wallX, wallY, wallW, wallH);

	}

	public Rectangle getRect() {
		return new Rectangle(wallX, wallY, wallW, wallH);
	}

}
