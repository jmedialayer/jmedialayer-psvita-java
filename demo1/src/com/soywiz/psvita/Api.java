package com.soywiz.psvita;

import com.jtransc.JTranscSystem;

import java.util.Arrays;

@SuppressWarnings("PointlessBitwiseExpression")
public abstract class Api {
	static public final int WIDTH = 480 * 2;
	static public final int HEIGHT = 272 * 2;

	static public Api create() {
		if (JTranscSystem.isJTransc()) {
			if (JTranscSystem.isCpp()) {
				return new ApiPsp2();
			} else {
				return new ApiJsCanvas();
			}
		} else {
			try {
				return (Api) Class.forName("com.soywiz.psvita.ApiGwt").newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	protected Api() {
	}

	abstract public void init_video();

	abstract public void end_video();

	abstract public void clear_screen();

	abstract public void swap_buffers();

	abstract public void draw_pixel(int x, int y, int color);

	public void draw_rectangle(int x, int y, int w, int h, int color) {
		int[] colors = new int[w * h];
		Arrays.fill(colors, color);
		draw_pixels(x, y, colors, w, h);
	}

	abstract public void font_draw_string(int x, int y, int color, String str);

	abstract protected void input_read_internal();

	public void input_read() {
		input_read_internal();
		double now = JTranscSystem.fastTime();
		for (Button button : BUTTONS) button.frame(now);
	}

	public int pad_buttons;

	abstract public void frame_end();

	abstract public void sceDisplayWaitVblankStart();

	abstract public void sceKernelExitProcess(int value);

	public final int PSP2_CTRL_SELECT = 1 << 0;
	public final int PSP2_CTRL_START = 1 << 3;
	public final int PSP2_CTRL_UP = 1 << 4;
	public final int PSP2_CTRL_RIGHT = 1 << 5;
	public final int PSP2_CTRL_DOWN = 1 << 6;
	public final int PSP2_CTRL_LEFT = 1 << 7;
	public final int PSP2_CTRL_LTRIGGER = 1 << 8;
	public final int PSP2_CTRL_RTRIGGER = 1 << 9;
	public final int PSP2_CTRL_TRIANGLE = 1 << 12;
	public final int PSP2_CTRL_CIRCLE = 1 << 13;
	public final int PSP2_CTRL_CROSS = 1 << 14;
	public final int PSP2_CTRL_SQUARE = 1 << 15;
	public final int PSP2_CTRL_ANY = 1 << 16;

	public void loop(Step step) {
		while (true) {
			input_read();
			clear_screen();
			step.step();
			swap_buffers();
			sceDisplayWaitVblankStart();
		}
	}

	public void draw_pixels(int x, int y, int[] colors, int width, int height) {
		int n = 0;
		for (int my = 0; my < height; my++) {
			for (int mx = 0; mx < width; mx++) {
				draw_pixel(x + mx, y + my, colors[n++]);
			}
		}
	}

	static public interface Step {
		public void step();
	}

	//public double[] BUTTON_START_PRESSING_TIME = new double[32];
	public double[] BUTTON_REPORTED_PRESSED_TIME = new double[32];
	public int[] BUTTON_PRESSING_COUNT = new int[32];

	public Button SELECT = new Button(0);
	public Button START = new Button(3);
	public Button UP = new Button(4);
	public Button RIGHT = new Button(5);
	public Button DOWN = new Button(6);
	public Button LEFT = new Button(7);
	public Button LTRIGGER = new Button(8);
	public Button RTRIGGER = new Button(9);
	public Button TRIANGLE = new Button(12);
	public Button CIRCLE = new Button(13);
	public Button CROSS = new Button(14);
	public Button SQUARE = new Button(15);
	public Button ANY = new Button(16);

	public Button[] BUTTONS = {SELECT, START, UP, RIGHT, DOWN, LEFT, LTRIGGER, RTRIGGER, TRIANGLE, CIRCLE, CROSS, SQUARE, ANY};

	public class Button {
		int bit;
		int mask;
		private boolean pressed;

		Button(int bit) {
			this.bit = bit;
			this.mask = (1 << bit);
		}

		void frame(double now) {
			pressed = false;
			if (isPressing()) {
				int count = BUTTON_PRESSING_COUNT[bit];
				double time = 20.0;
				if (count < 20) time = 40;
				if (count < 10) time = 75;
				if (count < 5) time = 150;
				if ((now - BUTTON_REPORTED_PRESSED_TIME[bit]) >= time) {
					BUTTON_REPORTED_PRESSED_TIME[bit] = now;
					BUTTON_PRESSING_COUNT[bit]++;
					pressed = true;
				}
			} else {
				BUTTON_PRESSING_COUNT[bit] = 0;
			}
		}

		public boolean pressed() {
			return pressed;
		}

		public boolean isPressing() {
			return (pad_buttons & mask) != 0;
		}

		public boolean isNotPressing() {
			return !isPressing();
		}
	}

}
