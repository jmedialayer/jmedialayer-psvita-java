import com.jtransc.annotation.JTranscMethodBody
import com.jtransc.util.JTranscBase64
import com.soywiz.psvita.Api
import java.io.File

object MainKotlin {
	@JvmStatic fun main(args: Array<String>) {
		val api = Api.create()

		api.init_video()

		val screen = Screen(BaseComponent())

		screen.setMainComponent(GameSelectorComponent(screen, api))

		api.loop {
			screen.step(api)
		}
	}

	@JTranscMethodBody(target = "cpp", value = """
		char c_name[1024] = {0};
		char c_mode[1024] = {0};

		::strcpy(c_name, N::istr3(p0).c_str());
		::strcpy(c_mode, N::istr3(p1).c_str());

		return (int64_t)(void *)::fopen(c_name, c_mode);
	""")
	external fun fopen(str: String, mode: String): Long

	@JTranscMethodBody(target = "cpp", value = """
		struct stat stat_buf = {0};
		char name[1024] = {0};
		::strcpy(name, N::istr3(p0).c_str());
		int rc = ::stat(name, &stat_buf);
		return (rc == 0) ? stat_buf.st_size : -1;
	""")
	external fun length(str: String): Long

	@JTranscMethodBody(target = "cpp", value = """
		return ::fseek((FILE *)(void *)p0, p1, p2);
	""")
	external fun fseek(file: Long, offset: Long, origin: Int): Int

	@JTranscMethodBody(target = "cpp", value = """
		return ::ftell((FILE *)(void *)p0);
	""")
	external fun ftell(file: Long): Int

	@JTranscMethodBody(target = "cpp", value = """
		::fclose((FILE *)(void *)p0);
	""")
	external fun fclose(file: Long): Unit

	const val SEEK_SET = 0
	const val SEEK_CUR = 1
	const val SEEK_END = 2
}

class GameSelectorComponent(val screen: Screen, val api: Api) : Component {
	val component = ListSelectorComponent().apply {
		elements = arrayListOf(
			"CHIP8 PONG" to {
				val chip8 = Chip8()
				chip8.executeProgram(api, JTranscBase64.decode("""
							agJrDGw/bQyi6tq23NZuACLUZgNoAmBg8BXwBzAAEhrHF3cIaf+i8NZxourattzW
							YAHgoXv+YATgoXsCYB+LAtq2YAzgoX3+YA3goX0CYB+NAtzWovDWcYaEh5RgP4YC
							YR+HEkYCEnhGPxKCRx9p/0cAaQHWcRIqaAJjAYBwgLUSimj+YwqAcIDVPwESomEC
							gBU/ARK6gBU/ARLIgBU/ARLCYCDwGCLUjjQi1GY+MwFmA2j+MwFoAhIWef9J/mn/
							Esh5AUkCaQFgBPAYdgFGQHb+Emyi8v4z8mXxKWQUZQDUVXQV8inUVQDugICAgICA
							gAAAAAAA
						"""), false)
				chip8.onExit {
					screen.setMainComponent(this@GameSelectorComponent)
				}
				screen.setMainComponent(chip8)
			},
			"DEMO" to {
				val demo = DemoComponent(screen)
				demo.onExit {
					screen.setMainComponent(this@GameSelectorComponent)
				}
				screen.setMainComponent(demo)
			},
			"EXIT" to {
				api.end_video()
				api.sceKernelExitProcess(0)
			}
		)
	}

	override fun update(api: Api) {
		component.update(api)
	}

	override fun draw(api: Api, x: Int, y: Int) {
		component.draw(api, x, y)
	}
}

class DemoComponent(val screen: Screen) : Component {
	private val WHITE = 0xFFFFFFFF.toInt()
	private val RED = 0xFF0000FF.toInt()
	private val PINK = 0xFFFF00FF.toInt()
	private val YELLOW = 0xFF00FFFF.toInt()
	private val BLACK = 0xFF000000.toInt()

	val onExit = Signal<Int>()

	var x = 0
	var y = 0
	var frame = 0

	val absolutePath = File("app0:/eboot.bin").absolutePath

	override fun update(api: Api) {
		if (api.START.pressed()) onExit(0)
		if (api.LEFT.isPressing) x--
		if (api.RIGHT.isPressing) x++
		if (api.UP.isPressing) y--
		if (api.DOWN.isPressing) y++

		if (x < 0) x = 0;
		if (y < 0) y = 0;
	}

	override fun draw(api: Api, x: Int, y: Int) {
		api.run {
			draw_rectangle(this@DemoComponent.x, this@DemoComponent.y, 100, 100, RED)
			draw_rectangle(100, 100, 100, 100, PINK)

			//val str = String.format("PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! %d %s", frame, files)
			//val str = String.format("PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! %d %s", frame, ":)")
			//val str = "PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! $frame :)\n$absolutePath : $readedEbootBinSize"
			val str = "PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! $frame :)\n$absolutePath\n${System.currentTimeMillis()}"

			font_draw_string(1, 1, BLACK, str)
			font_draw_string(0, 0, YELLOW, str)
			frame++
		}
	}
}