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

	// �൱��һ���ܹܣ����ಿ����Ҫ���������ͨ����������ܹܵ�һ����������������tank��missile�е�tc����
	// mediator ��ͣ�� ���ģʽ

	private static final long serialVersionUID = 1L;
	public static final int KILL_SCORE = 1;
	public static final int EAT_SCORE = -1;
	public static final int START_X=500;
	public static final int START_Y=500;	//	������λ
	
	private int bloodcount = 0;
	private int score = 0;
	
	private JTextField ruleDisplay;
	private JButton ruleButton;
	private String ruleString="";
	
	private void componentAddListener(){
		this.ruleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, ruleString, "��Ϸ����", JOptionPane.PLAIN_MESSAGE);
			}
		});
	}

	// ע��x��y��ķ��������Ͻ�Ϊԭ�㣬x�����ң�y������
	// ����̹�˶�������ֻ��Ҫ�ı���������Ĳο���Ϳ����ˣ�fillOval��ǰ��������x��y������Ȼ����Ҫx��y�ĸı䣬��ô��Ӧ�ð�x��y����Ϊ����
	/*
	 * ̹�˶������� 1���ο����Ϊ���� 2�������̲߳����ػ� 3��ÿ���ػ��ı�tankλ��
	 */

	Tank myTank = new Tank(START_X, START_Y, true, Tank.Direction.STOP, this);
	

	List<Missile> missiles = new ArrayList<Missile>(); // �����ѭ�������ȽϿ죬��Ϊÿ�ζ�Ҫ������ȫ�����ӵ�Ȼ���ػ���
	List<Tank> enemyTanks = new ArrayList<Tank>();
	List<Explode> explodes = new ArrayList<Explode>();
	Image offScreenImage = null; // ����ͼƬ
	Blood b = new Blood();

	Wall w1 = new Wall(100, 200, 20, 150, this);
	Wall w2 = new Wall(300, 100, 300, 20, this);
	
	public void enemyRestart(){
		missiles.clear();
		enemyTanks.clear();//�Ȱ�֮ǰ�����
		for (int i = 0; i < 10; i++) {
			enemyTanks.add(new Tank(50 + 40 * (i + 1), 50, false, Tank.Direction.D, this));
		}
	}

	@Override
	public void paint(Graphics g) { // ��������ڴ����ػ���ʱ����Զ�����
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
			t.collidesWithWall(w2); // ����̹�˿��Դ�ǽ��
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
		myTank.tankDraw(g); // ���٣�������˵����ֻҪ������һ�����Լ��Ľӿڣ�Ȼ���㰮��ô������ô����
	}


	@Override
	public void update(Graphics g) {
		if (offScreenImage == null) { // ������Ͳ��ô����ˣ������ظ�������
			offScreenImage = this.createImage(Constant.FRAME_X, Constant.FRAME_Y);
		}
		// ����ͼƬҲ��һ֧���ʣ�����Ҫ����������.���ʾ���Graphics
		Graphics gOffScreen = offScreenImage.getGraphics();
		Color c = gOffScreen.getColor();
		gOffScreen.setColor(Color.green);
		gOffScreen.fillRect(0, 0, Constant.FRAME_X, Constant.FRAME_Y);
		gOffScreen.setColor(c);
		paint(gOffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
	}

	public void launchFrame() { // �����������ƴ��ڵģ�ֻ��һ���������paint�ǻ��ƴ������������
		enemyRestart();
		this.setLocation(400, 300); // ���ڵ�λ��
		this.setSize(Constant.FRAME_X, Constant.FRAME_Y); // ���ڴ�С
		this.setTitle(Constant.TITLE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // �����࣬���ڼ�̵ģ�����չ�ԵĶ�С�ࡣ��д�����еķ�����ѡ�ж����Ҽ�-Դ��-��д��ѡ����Ҫ��д�ķ���

		});
		this.setResizable(false); // �������ڸı��С
		this.setBackground(Color.green);

		this.addKeyListener(new KeyMonitor()); // ����Ҫ�Ѽ������ӵ���Ҫ��ִ�й��ܵ�frame�в���ִ����Ӧ�ļ������ܡ�
		
//		this.ruleButton=new JButton("��Ϸ����");
//		ruleButton.setLocation(10,50);
//		this.add(ruleButton);
		
		setVisible(true);
		new Thread(new PaintThread()).start(); // �������߳�һ��Ҫ���������ڻ��ƴ��ڵ�ʱ�����Ϳ����ˡ�
	}

	private class PaintThread implements Runnable { // �ڲ��࣬���ڲ����㹫�����࣬��������Ч�ص����ⲿ��ı����ͷ�����ֻΪ��װ������������Ϊ�ڲ���

		@Override
		public void run() {
			while (true) {
				repaint(); // �����ⲿ��װ���repaint���ڲ�����Ժܷ���ĵ����ⲿ��װ��ĳ�Ա�����ͳ�Ա������
				try {
					Thread.sleep(Constant.SLEEPTIME); // ÿ��100ms���̶߳�һ�Σ������������ػ�һ�Σ�repaint����ÿ���ػ�y��ֵ����ͬ������̹�˶�������
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class KeyMonitor extends KeyAdapter {
		// �̳����ʵ�ֽӿڵ��ŵ�:�����ĵķ������Բ���

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
