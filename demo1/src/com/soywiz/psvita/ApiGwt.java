package com.soywiz.psvita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class ApiGwt extends Api {
	JFrame frame = new JFrame("psvita");
	int width = WIDTH;
	int height = HEIGHT;
	//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	//GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	//VolatileImage image = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);

	BufferedImage front = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D frontg = front.createGraphics();
	Graphics2D g = image.createGraphics();
	boolean[] keys = new boolean[KeyEvent.KEY_LAST];

	public ApiGwt() {

	}

	private Color convertColor(int color) {
		int r = (color >> 0) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 16) & 0xFF;
		int a = (color >> 24) & 0xFF;
		return new Color(r, g, b, a);
	}

	@Override
	public void init_video() {
		JLabel label = new JLabel(new ImageIcon(front));
		label.setSize(width, height);
		frame.add(label);
		//frame.setSize(width, height);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				keys[e.getKeyCode() & 0x3FF] = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				keys[e.getKeyCode() & 0x3FF] = false;
			}
		});
	}

	@Override
	public void end_video() {
		frame.setVisible(false);
		frame.dispose();
	}

	@Override
	public void clear_screen() {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
	}

	@Override
	public void swap_buffers() {
		frontg.drawImage(image, 0, 0, null);
		frame.repaint();
	}

	@Override
	public void draw_pixel(int x, int y, int color) {
		//g.draw
		image.setRGB(x, y, color);
	}

	@Override
	public void draw_rectangle(int x, int y, int w, int h, int color) {
		//image.set
		//image.setRGB(x, y, color);
		g.setColor(convertColor(color));
		g.fillRect(x, y, w, h);
	}

	@Override
	public void font_draw_string(int x, int y, int color, String str) {
		g.setColor(convertColor(color));
		Font f = new Font("Lucida Console", Font.PLAIN, 16);
		g.setFont(f);
		FontMetrics fontMetrics = g.getFontMetrics();
		g.drawString(str, x, y + fontMetrics.getHeight());
	}

	@Override
	protected void input_read_internal() {
		pad_buttons = 0;
		if (keys[KeyEvent.VK_ENTER]) pad_buttons |= PSP2_CTRL_START;
		if (keys[KeyEvent.VK_SPACE]) pad_buttons |= PSP2_CTRL_SELECT;
		if (keys[KeyEvent.VK_W]) pad_buttons |= PSP2_CTRL_TRIANGLE;
		if (keys[KeyEvent.VK_S]) pad_buttons |= PSP2_CTRL_CROSS;
		if (keys[KeyEvent.VK_A]) pad_buttons |= PSP2_CTRL_SQUARE;
		if (keys[KeyEvent.VK_D]) pad_buttons |= PSP2_CTRL_CIRCLE;
		if (keys[KeyEvent.VK_UP]) pad_buttons |= PSP2_CTRL_UP;
		if (keys[KeyEvent.VK_DOWN]) pad_buttons |= PSP2_CTRL_DOWN;
		if (keys[KeyEvent.VK_LEFT]) pad_buttons |= PSP2_CTRL_LEFT;
		if (keys[KeyEvent.VK_RIGHT]) pad_buttons |= PSP2_CTRL_RIGHT;
	}

	@Override
	public void frame_end() {
	}

	private long lastFrame = System.currentTimeMillis();

	@Override
	public void sceDisplayWaitVblankStart() {
		try {
			while (System.currentTimeMillis() - lastFrame < 16) {
				Thread.sleep(1L);
			}

			lastFrame = System.currentTimeMillis();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sceKernelExitProcess(int value) {
		System.exit(value);
	}
}
