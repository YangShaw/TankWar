import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.*;

import java.util.ArrayList;

public class TankClient extends Frame {

	// 相当于一个总管，其余部分想要做事情可以通过持有这个总管的一个引用来做（比如tank和missile中的tc）。
	// mediator 调停者 设计模式

	private static final long serialVersionUID = 1L;
	public static final int KILL_SCORE = 1;
	public static final int EAT_SCORE = -1;
	public static final int START_X=500;
	public static final int START_Y=500;	//	出生点位
	
	private int bloodcount = 0;
	private int score = 0;
	
	private JTextField ruleDisplay;
	private JButton ruleButton;
	private String ruleString="";
	
	private void componentAddListener(){
		this.ruleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, ruleString, "游戏规则", JOptionPane.PLAIN_MESSAGE);
			}
		});
	}

	// 注意x，y轴的方向。以左上角为原点，x轴向右，y轴向下
	// 想让坦克动起来，只需要改变矩形依赖的参考点就可以了（fillOval的前两个参数x，y），既然我需要x，y的改变，那么我应该把x，y设置为变量
	/*
	 * 坦克动起来： 1、参考点改为变量 2、启动线程不断重画 3、每次重画改变tank位置
	 */

	Tank myTank = new Tank(START_X, START_Y, true, Tank.Direction.STOP, this);
	

	List<Missile> missiles = new ArrayList<Missile>(); // 数组的循环遍历比较快，因为每次都要读里面全部的子弹然后重画。
	List<Tank> enemyTanks = new ArrayList<Tank>();
	List<Explode> explodes = new ArrayList<Explode>();
	Image offScreenImage = null; // 虚拟图片
	Blood b = new Blood();

	Wall w1 = new Wall(100, 200, 20, 150, this);
	Wall w2 = new Wall(300, 100, 300, 20, this);
	
	public void enemyRestart(){
		missiles.clear();
		enemyTanks.clear();//先把之前的清空
		for (int i = 0; i < 10; i++) {
			enemyTanks.add(new Tank(50 + 40 * (i + 1), 50, false, Tank.Direction.D, this));
		}
	}

	@Override
	public void paint(Graphics g) { // 这个方法在窗口重画的时候会自动调用
		bloodcount++;
		g.drawString("Score:" + this.score, 10, 70);
		g.drawString("Enemytanks Count:" + enemyTanks.size(), 10, 90);
		g.drawString("MyTank's power:" + myTank.getPowerCount(), 10, 110);
		g.drawString("MyTank's supermissile:" + myTank.getSuperMissile(), 10, 130);

		if (enemyTanks.size() <= 0) {
//			missiles.clear();
			enemyRestart();
		}

		for (int i = 0; i < missiles.size(); i++) {
			Missile m = missiles.get(i);
			if (m.hitTanks(enemyTanks)) {
				score = score + KILL_SCORE;
			}
			m.hitTank(myTank);
			m.hitWall(w1);
			m.hitWall(w2);
			m.missileDraw(g);

		}
		for (int i = 0; i < explodes.size(); i++) {
			Explode e = explodes.get(i);
			e.explodeDraw(g);
		}
		for (int i = 0; i < enemyTanks.size(); i++) {
			Tank t = enemyTanks.get(i);
			t.collidesWithWall(w1);
			t.collidesWithWall(w2); // 己方坦克可以穿墙。
			t.collidesWithTanks(enemyTanks);
			t.tankDraw(g);

		}
		if (bloodcount == 3000) {
			b = new Blood();
			bloodcount = 0;
		}
		b.bloodDraw(g);
		w1.wallDraw(g);
		w2.wallDraw(g);
		if (myTank.eatBlood(b)) {
			score = score + EAT_SCORE;
		}
		myTank.tankDraw(g); // 精辟，对我来说，你只要告诉我一个画自己的接口，然后你爱怎么画就怎么画。
	}


	@Override
	public void update(Graphics g) {
		if (offScreenImage == null) { // 创建完就不用创建了，避免重复创建。
			offScreenImage = this.createImage(Constant.FRAME_X, Constant.FRAME_Y);
		}
		// 这张图片也有一支画笔，首先要获得这个画笔.画笔就是Graphics
		Graphics gOffScreen = offScreenImage.getGraphics();
		Color c = gOffScreen.getColor();
		gOffScreen.setColor(Color.green);
		gOffScreen.fillRect(0, 0, Constant.FRAME_X, Constant.FRAME_Y);
		gOffScreen.setColor(c);
		paint(gOffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
	}

	public void launchFrame() { // 这个是用来设计窗口的，只是一个框，上面的paint是绘制窗口里面的内容
		enemyRestart();
		this.setLocation(400, 300); // 存在的位置
		this.setSize(Constant.FRAME_X, Constant.FRAME_Y); // 窗口大小
		this.setTitle(Constant.TITLE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // 匿名类，用于简短的，无扩展性的短小类。重写父类中的方法，选中对象，右键-源码-改写，选中需要改写的方法

		});
		this.setResizable(false); // 不允许窗口改变大小
		this.setBackground(Color.green);

		this.addKeyListener(new KeyMonitor()); // 必须要把监听器加到需要它执行功能的frame中才能执行相应的监听功能。
		
//		this.ruleButton=new JButton("游戏规则");
//		ruleButton.setLocation(10,50);
//		this.add(ruleButton);
		
		setVisible(true);
		new Thread(new PaintThread()).start(); // 创建了线程一定要开启啊！在绘制窗口的时候开启就可以了。
	}

	private class PaintThread implements Runnable { // 内部类，用于不方便公开的类，但可以有效地调用外部类的变量和方法。只为包装类服务的类设置为内部类

		@Override
		public void run() {
			while (true) {
				repaint(); // 调用外部包装类的repaint。内部类可以很方便的调用外部包装类的成员变量和成员方法。
				try {
					Thread.sleep(Constant.SLEEPTIME); // 每隔100ms，线程动一次，即窗口内容重画一次（repaint），每次重画y的值都不同，所以坦克动起来。
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class KeyMonitor extends KeyAdapter {
		// 继承相比实现接口的优点:不关心的方法可以不用

		@Override
		public void keyReleased(KeyEvent e) {
			myTank.tankKeyReleased(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			myTank.tankKeyPressed(e);
		}

	}
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
}
