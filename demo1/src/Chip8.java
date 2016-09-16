import com.jtransc.util.JTranscBase64;
import com.soywiz.psvita.Api;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Chip8 implements Component {
	static private final int PROGRAM_START_ADDR = 0x0200;
	static private final int DELAY_MS = 17;
	//static private final boolean DEBUG = true;
	static private final boolean DEBUG = false;
	public int PC = PROGRAM_START_ADDR;
	public int I;
	public byte[] memory = new byte[0x1000];
	public byte[] V = new byte[0x10];
	public int callstackPos = 0;
	public int[] callstack = new int[96];
	public int delay_timer = 0;
	public int sound_timer = 0;
	public long last_delay_timer_time = 0L;
	public Screen screen = new Screen();
	static private byte[] FONT = JTranscBase64.decode("8JCQkPAgYCAgcPAQ8IDw8BDwEPCQkPAQEPCA8BDw8IDwkPDwECBAQPCQ8JDw8JDwEPDwkPCQkOCQ4JDg8ICAgPDgkJCQ4PCA8IDw8IDwgIA=");
	public Signal<Integer> onExit = new Signal<Integer>();

	private int V(int index) {
		return V[index] & 0xFF;
	}

	@Override
	public void update(@NotNull Api api) {
		for (int n = 0; n < 10; n++) execute();
		long now = System.currentTimeMillis();
		if (now - last_delay_timer_time >= DELAY_MS) {
			last_delay_timer_time = now;
			if (delay_timer > 0) delay_timer--;
		}
		if (api.START.pressed()) {
			onExit.invoke(0);
		}
	}

	@Override
	public void draw(@NotNull Api api, int x, int y) {
		api.draw_pixels(0, 0, screen.colors, 64, 32);
	}

	public void executeProgram(final Api api, byte[] data, boolean mainLoop) {
		//api.clear_screen();
		//api.swap_buffers();
		System.out.println("Program: " + data.length);
		reset();
		loadProgram(data);
		if (mainLoop) {
			api.loop(new Api.Step() {
				@Override
				public void step() {
					update(api);
					draw(api, 0, 0);
				}
			});
		}
	}

	public void reset() {
		PC = PROGRAM_START_ADDR;
		I = 0;
		Arrays.fill(memory, (byte) 0);
		Arrays.fill(V, (byte) 0);
		callstackPos = 0;
		screen.clear();
		System.arraycopy(FONT, 0, memory, 0, FONT.length);
	}

	public void loadProgram(byte[] data) {
		System.arraycopy(data, 0, memory, PROGRAM_START_ADDR, data.length);
	}

	static public class Screen {
		boolean[] data = new boolean[64 * 32];
		int[] colors = new int[64 * 32];

		public boolean check(int x, int y) {
			return x >= 0 && y >= 0 && x < 64 && y < 32;
		}

		public int index(int x, int y) {
			return y * 64 + x;
		}

		public boolean toggle(int x, int y, boolean value) {
			boolean result = get(x, y) ^ value;
			set(x, y, result);
			return result;
		}

		public void set(int x, int y, boolean value) {
			if (!check(x, y)) return;
			int index = index(x, y);
			data[index] = value;
			colors[index] = value ? 0xFFFFFFFF : 0xFF000000;
		}

		public boolean get(int x, int y) {
			if (!check(x, y)) return false;
			return data[index(x, y)];
		}

		public void clear() {
			Arrays.fill(data, false);
			Arrays.fill(colors, 0xFF000000);
		}
	}

	public void execute() {
		int b0 = memory[PC++] & 0xFF;
		int b1 = memory[PC++] & 0xFF;

		int i = (b0 << 8) | b1;

		int I = b0 >>> 4;
		int X = b0 & 0xF;
		int Y = b1 >>> 4;
		int NNN = i & 0xFFF;
		int NN = b1 & 0xFF;
		int N = b1 & 0xF;

		//System.out.println("Instruction: " + I);

		switch (I) {
			case 0x0: { // Calls RCA 1802 program at address NNN. Not necessary for most ROMs.
				if (DEBUG)
					System.out.printf("CALL_RCA 0x%03X // Calls RCA 1802 program at address NNN. Not necessary for most ROMs.\n", NNN);
				switch (NNN) {
					case 0x00E0: // Clears the screen.
						clear_the_screen();
						break;
					case 0x00EE: // Returns from a subroutine.
						PC = callstack[--callstackPos];
						break;
					default:
						throw new RuntimeException("Not implemented");
				}
				break;
			}
			case 0x1: { // Jumps to address NNN.
				if (DEBUG) System.out.printf("JUMP 0x%03X // Jumps to address NNN.\n", NNN);
				PC = NNN;
				break;
			}
			case 0x2: { // Calls subroutine at NNN.
				if (DEBUG) System.out.printf("CALL 0x%03X // Calls subroutine at NNN.\n", NNN);
				callstack[callstackPos++] = PC;
				PC = NNN;
				break;
			}
			case 0x3: { // Skips the next instruction if VX equals NN.
				if (DEBUG)
					System.out.printf("SKIP_EQ V%X==0x%02X // Skips the next instruction if VX equals NN.\n", X, NN);
				if (V(X) == b1) PC += 2;
				break;
			}
			case 0x4: { // Skips the next instruction if VX doesn't equal NN.
				if (DEBUG)
					System.out.printf("SKIP_EQ V%X!=0x%02X // Skips the next instruction if VX doesn't equal NN.\n", X, NN);
				if (V(X) != b1) PC += 2;
				break;
			}
			case 0x5: { // Skips the next instruction if VX equals VY.
				if (DEBUG) System.out.printf("SKIP_EQ V%X==V%X // Skips the next instruction if VX equals VY.\n", X, Y);
				if (V(X) == V(Y)) PC += 2;
				break;
			}
			case 0x6: { // Sets VX to NN.
				if (DEBUG) System.out.printf("V%X = 0x%02X // Sets VX to NN.\n", X, NN);
				V[X] = (byte) NN;
				break;
			}
			case 0x7: { // Adds NN to VX.
				if (DEBUG) System.out.printf("V%X += 0x%02X // Adds NN to VX.\n", X, NN);
				V[X] += NN;
				break;
			}
			case 0x8: {
				int y = V(Y);
				switch (b1 & 0xF) {
					case 0x0: { // Sets VX to the value of VY.
						if (DEBUG) System.out.printf("V%X = V%X // Sets VX to the value of VY.\n", X, Y);
						V[X] = (byte) y;
						break;
					}
					case 0x1: { // Sets VX to VX or VY.
						if (DEBUG) System.out.printf("V%X |= V%X // Sets VX to VX or VY.\n", X, Y);
						V[X] |= y;
						break;
					}
					case 0x2: { // Sets VX to VX and VY.
						if (DEBUG) System.out.printf("V%X &= V%X // Sets VX to VX and VY.\n", X, Y);
						V[X] &= y;
						break;
					}
					case 0x3: { // Sets VX to VX xor VY.
						if (DEBUG) System.out.printf("V%X ^= V%X // Sets VX to VX xor VY.\n", X, Y);
						V[X] ^= y;
						break;
					}
					case 0x4: { // Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
						if (DEBUG)
							System.out.printf("... // Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.\n");
						int result = V(X) + V(Y);
						V[X] = (byte) result;
						boolean carry = (result & 0xFF) != result;
						V[15] = (byte) (carry ? 1 : 0);
						break;
					}
					case 0x5: { // VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
						if (DEBUG)
							System.out.printf("... // VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.\n");

						int resultValue;
						if (V(X) > V(Y)) {
							resultValue = V(X) - V(Y);
							V[0xF] = 1;
						} else {
							resultValue = 256 + V[X] - V[Y];
							V[0xF] = 0;
						}
						V[X] = (byte) resultValue;

						break;
					}
					case 0x6: { // Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.[2]
						if (DEBUG)
							System.out.printf("... // Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.[2]\n");
						V[X] = (byte) (V[X] >>> 1);
						V[15] = (byte) (V(X) & 1);
						break;
					}
					case 0x7: { // Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
						if (DEBUG)
							System.out.printf("... // Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.\n");
						int result = V(Y) - V(X);
						V[X] = (byte) result;
						V[15] = (byte) ((result < 0) ? 1 : 0);
						break;
					}
					case 0xE: { // Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.[2]
						if (DEBUG)
							System.out.printf("... // Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.[2]\n");
						V[X] = (byte) (V[X] << 1);
						V[15] = (byte) (V(X) >>> 7);
						break;
					}
				}
				break;
			}
			case 0x9: { // Skips the next instruction if VX doesn't equal VY.
				if (DEBUG)
					System.out.printf("SKIP_IF V%X != V%X // Skips the next instruction if VX doesn't equal VY.\n", X, Y);
				if (V(X) != V(Y)) PC += 2;
				break;
			}
			case 0xA: { // Sets I to the address NNN.
				if (DEBUG) System.out.printf("I = 0x%03X // Sets I to the address NNN.\n", NNN);
				this.I = NNN;
				break;
			}
			case 0xB: { // Jumps to the address NNN plus V0.
				if (DEBUG) System.out.printf("JUMP %03X+V0 // Jumps to the address NNN plus V0.\n", NNN);
				PC = NNN + V(0);
				break;
			}
			case 0xC: { // Sets VX to the result of a bitwise and operation on a random number and NN.
				if (DEBUG) System.out.printf("V%X = RAND() // Jumps to the address NNN plus V0.\n", X);
				V[X] = (byte) (Math.random() * NN);
				break;
			}
			case 0xD: {
				// Draws a sprite at coordinate (VX, VY) that has
				// a width of 8 pixels and a height of N pixels.
				// Each row of 8 pixels is read as bit-coded starting
				// from memory location I; I value doesn’t change after the execution of this instruction. As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen
				int x = V[X];
				int y = V[Y];
				int width = 8;
				int height = N;

				if (DEBUG) System.out.printf("DRAW(%d,%d,%d,%d)\n", x, y, width, height);

				boolean collided = draw(x, y, width, height);
				V[15] = (byte) (collided ? 1 : 0);
				break;
			}
			case 0xE: {
				switch (NN) {
					case 0x9E: { // Skips the next instruction if the key stored in VX is pressed.
						if (DEBUG)
							System.out.printf("... // Skips the next instruction if the key stored in VX is pressed.\n");
						if (is_pressed(V(X))) PC += 2;
						break;
					}
					case 0xA1: { // Skips the next instruction if the key stored in VX isn't pressed.
						if (DEBUG)
							System.out.printf("... // Skips the next instruction if the key stored in VX isn't pressed.\n");
						if (!is_pressed(V(X))) PC += 2;
						break;
					}
				}
				break;
			}
			case 0xF: {
				switch (NN) {
					case 0x07: { // Sets VX to the value of the delay timer.
						if (DEBUG) System.out.printf("V%X = delay_timer\n", X);
						V[X] = (byte) delay_timer;
						break;
					}
					case 0x0A: { // A key press is awaited, and then stored in VX.
						if (DEBUG) System.out.printf("... // A key press is awaited, and then stored in VX.\n");
						V[X] = (byte) wait_keypress();
						break;
					}
					case 0x15: { // Sets the delay timer to VX.
						if (DEBUG) System.out.printf("... // Sets the delay timer to VX.\n");
						this.delay_timer = V(X);
						break;
					}
					case 0x18: { // Sets the sound timer to VX.
						if (DEBUG) System.out.printf("... // Sets the sound timer to VX.\n");
						this.sound_timer = V(X);
						break;
					}
					case 0x1E: { // Adds VX to I
						if (DEBUG) System.out.printf("... // Adds VX to I\n");
						this.I += V(X);
						break;
					}
					case 0x029: {
						if (DEBUG)
							System.out.printf("... // Sets I to the location of the sprite for the character in VX.\n");
						// Sets I to the location of the sprite for the character in VX.
						// Characters 0-F (in hexadecimal) are represented by a 4x5 font.
						this.I = V(X) * 5;
						break;
					}
					case 0x33: {
						if (DEBUG) System.out.printf("... // Stores the binary-coded decimal representation of VX,\n");
						// Stores the binary-coded decimal representation of VX,
						// with the most significant of three digits at the address
						// in I, the middle digit at I plus 1, and the least
						// significant digit at I plus 2. (In other words,
						// take the decimal representation of VX, place the
						// hundreds digit in memory at location in I, the tens
						// digit at location I+1, and the ones digit at location I+2.)

						memory[this.I + 0] = (byte) ((V(X) / 100) % 10);
						memory[this.I + 1] = (byte) ((V(X) / 10) % 10);
						memory[this.I + 2] = (byte) ((V(X) / 1) % 10);
						break;
					}
					case 0x55: { // Stores V0 to VX (including VX) in memory starting at address I.[4]
						if (DEBUG)
							System.out.printf("... // Stores V0 to VX (including VX) in memory starting at address I.[4]\n");
						throw new RuntimeException("Not implemented");
						//break;
					}
					case 0x65: { // Fills V0 to VX (including VX) with values from memory starting at address I.[4]
						if (DEBUG)
							System.out.printf("FILL V0..V%X // Fills V0 to VX (including VX) with values from memory starting at address I.[4]\n", X);
						int numRegisters = X;
						for (int counter = 0; counter <= numRegisters; counter++) {
							V[counter] = this.memory[this.I + counter];
						}
						break;
					}
				}
				break;
			}
			default:
				throw new RuntimeException("Not implemented " + I);
		}
	}

	void sleep(int n) {
		System.out.println("SLEEP: " + n);
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	int wait_keypress() {
		sleep(1000);
		return 0;
	}

	boolean is_pressed(int key) {
		return false;
	}

	private void clear_the_screen() {
		System.out.println("CLEAR");
	}

	private boolean draw(int x, int y, int width, int height) {
		if (DEBUG) System.out.println("DRAW: " + x + "," + y + "," + width + "," + height);
		/*
		System.out.printf("%08X\n", this.memory[this.I]);
		System.out.printf("%08X\n", this.memory[this.I + 1]);
		System.out.printf("%08X\n", this.memory[this.I + 2]);
		System.out.printf("%08X\n", this.memory[this.I + 3]);
		System.out.printf("%08X\n", this.memory[this.I + 4]);
		System.out.printf("%08X\n", this.memory[this.I + 5]);
		*/
		for (int ny = 0; ny < height; ny++) {
			int row = this.memory[this.I + ny] & 0xFF;
			if (DEBUG) System.out.printf("%02X\n", row);
			for (int nx = 0; nx < width; nx++) {
				this.screen.toggle(x + nx, y + ny, ((row >> (7 - nx)) & 1) != 0);
			}
		}
		return false;
	}
}
