import com.soywiz.psvita.Api

class Screen(var component: Component? = null) {
	fun setMainComponent(component: Component) {
		this.component = component
	}

	fun step(api: Api) {
		this.component?.update(api)
		this.component?.draw(api, 0, 0)
	}
}

interface Component {
	fun update(api: Api)
	fun draw(api: Api, x: Int, y: Int)
}

open class BaseComponent : Component {
	override fun draw(api: Api, x: Int, y: Int) {
	}

	override fun update(api: Api) {
	}
}

class Signal<T> {
	val handlers = arrayListOf<(T) -> Unit>()

	operator fun plusAssign(handler: (T) -> Unit) {
		handlers += handler
	}

	operator fun invoke(handler: (T) -> Unit) {
		handlers += handler
	}

	operator fun invoke(value: T) {
		for (handler in handlers) handler(value)
	}
}

class ListSelectorComponent : Component {
	var elements: List<Pair<String, () -> Unit>> = listOf()
	var index: Int = 0
	val selected = Signal<Int>()
	val changed = Signal<Int>()

	override fun update(api: Api) {
		val nextIndex = if (api.UP.pressed()) index - 1 else if (api.DOWN.pressed()) index + 1 else index
		if (nextIndex in elements.indices) {
			if (this.index != nextIndex) {
				this.index = nextIndex
				changed(this.index)
			}
		}
		if (api.CROSS.pressed() || api.START.pressed()) {
			selected(this.index)
			if (this.index in elements.indices) {
				elements[this.index].second()
			}
		}
	}

	override fun draw(api: Api, x: Int, y: Int) {
		var ay = 0
		for ((index, e) in elements.withIndex()) {
			val color = if (index == this.index) 0xFFFF00FF.toInt() else 0xFF000000.toInt()
			api.font_draw_string(x, y + ay, color, e.first)
			ay += 16
		}
	}
}