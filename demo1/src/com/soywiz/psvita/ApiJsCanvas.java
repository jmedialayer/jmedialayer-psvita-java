package com.soywiz.psvita;

import com.jtransc.annotation.JTranscAddFile;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.target.Js;

@JTranscAddMembers(target = "js", value = {
	"this.canvas = null;",
	"this.ctx = null;",
})
@JTranscAddFile(target = "js",  process = true, src = "js/index.html", dst = "index.html")
public class ApiJsCanvas extends Api {
	@Override
	public void init_video() {
		Js.v_raw("var _this = this;");
		Js.v_raw("var canvas = document.createElement('canvas');");
		Js.v_raw("canvas.width = 480 * 2;");
		Js.v_raw("canvas.height = 272 * 2;");
		Js.v_raw("document.body.appendChild(canvas);");
		Js.v_raw("this.canvas = canvas;");
		Js.v_raw("this.ctx = canvas.getContext('2d');");
		Js.v_raw("document.body.addEventListener('keyup', function(e) { _this['{% METHOD com.soywiz.psvita.ApiJsCanvas:handleKeyEvent %}'](false, e.keyCode); });");
		Js.v_raw("document.body.addEventListener('keydown', function(e) { _this['{% METHOD com.soywiz.psvita.ApiJsCanvas:handleKeyEvent %}'](true, e.keyCode); });");
	}

	// @TODO: This shouldn't be neccessary! since this is referenced in init_video Js.raw methods
	@JTranscKeep
	private void handleKeyEvent(boolean pressing, int keyCode) {
		System.out.println("handleKeyEvent:" + pressing + "," + keyCode);
		pad_buttons = 0;
		int mask = convertToMask(keyCode);
		if (pressing) {
			pad_buttons |= mask;
		} else {
			pad_buttons &= ~mask;
		}
	}

	private int convertToMask(int keyCode) {
		switch (keyCode) {
			case 13: return PSP2_CTRL_START; // RETURN
			case 32: return PSP2_CTRL_SELECT; // SPACE
			case 37: return PSP2_CTRL_LEFT; // LEFT
			case 38: return PSP2_CTRL_UP; // UP
			case 39: return PSP2_CTRL_RIGHT; // RIGHT
			case 40: return PSP2_CTRL_DOWN; // DOWN
			case 87: return PSP2_CTRL_TRIANGLE; // W
			case 64: return PSP2_CTRL_SQUARE; // A
			case 83: return PSP2_CTRL_CROSS; // S
			case 68: return PSP2_CTRL_CIRCLE; // D
			case 81: return PSP2_CTRL_LTRIGGER; // Q
			case 69: return PSP2_CTRL_RTRIGGER; // E
		}
		return 0;
	}

	@Override
	public void end_video() {

	}

	@Override
	public void clear_screen() {
		setColor(0xFFFFFFFF);
		Js.v_raw("this.ctx.fillRect(0, 0, 480 * 2, 272 * 2);");
	}

	@Override
	public void swap_buffers() {
	}

	@Override
	public void draw_pixel(int x, int y, int color) {
		draw_pixels(x, y, new int[] { color }, 1, 1);
	}

	@Override
	public void draw_pixels(int x, int y, int[] colors, int width, int height) {
		Js.v_raw("var data = this.ctx.createImageData(p3, p4);");
		Js.v_raw("var dd = data.data;");
		Js.v_raw("var area = p3 * p4;");
		Js.v_raw("for (var n = 0, m = 0; n < area; n++) {");
		Js.v_raw("	var c = p2.data[n];");
		Js.v_raw("	dd[m++] = (c >> 0) & 0xFF;");
		Js.v_raw("	dd[m++] = (c >> 8) & 0xFF;");
		Js.v_raw("	dd[m++] = (c >> 16) & 0xFF;");
		Js.v_raw("	dd[m++] = (c >> 24) & 0xFF;");
		Js.v_raw("}");
		Js.v_raw("this.ctx.putImageData(data, p0, p1);");
	}

	private void setColor(int r, int g, int b, int a) {
		Js.v_raw("this.ctx.fillStyle = 'rgba(' + p0 + ', ' + p1 + ', ' + p2 + ', ' + (p3 / 255.0) + ')';");
	}

	private void setColor(int color) {
		setColor(
			(color >> 0) & 0xFF,
			(color >> 8) & 0xFF,
			(color >> 16) & 0xFF,
			(color >> 24) & 0xFF
		);
	}

	@Override
	public void font_draw_string(int x, int y, int color, String str) {
		setColor(color);
		Js.v_raw("this.ctx.font = 'normal 16px Lucida Console';");
		Js.v_raw("this.ctx.textBaseline = 'top';");
		Js.v_raw("this.ctx.fillText(N.istr(p3), p0, p1);");
	}

	@Override
	protected void input_read_internal() {

	}

	@Override
	public void frame_end() {

	}

	@Override
	public void sceDisplayWaitVblankStart() {

	}

	@Override
	public void sceKernelExitProcess(int value) {

	}

	@Override
	public void loop(Step step) {
		Js.v_raw("var _this = this;");
		Js.v_raw("function one() {");
		Js.v_raw("	_this['{% METHOD com.soywiz.psvita.ApiJsCanvas:clear_screen %}']();");
		Js.v_raw("	_this['{% METHOD com.soywiz.psvita.ApiJsCanvas:input_read %}']();");
		Js.v_raw("	p0['{% METHOD com.soywiz.psvita.Api@Step:step %}']();");
		Js.v_raw("	requestAnimationFrame(one);");
		Js.v_raw("};");
		Js.v_raw("one();");
	}
}
