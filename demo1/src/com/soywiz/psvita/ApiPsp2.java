package com.soywiz.psvita;

import com.jtransc.annotation.JTranscAddFile;
import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscAddLibraries;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.io.JTranscSyncIO;
import com.jtransc.target.Cpp;
import com.jtransc.time.JTranscClock;

import java.io.FileNotFoundException;

@JTranscAddHeader(target = "cpp", value = {
	"extern \"C\" {",
	"#include <psp2/io/dirent.h>",
	"#include <psp2/ctrl.h>",
	"#include <psp2/touch.h>",
	"#include <psp2/display.h>",
	"#include <psp2/gxm.h>",
	"#include <psp2/types.h>",
	"#include <psp2/moduleinfo.h>",
	"#include <psp2/kernel/processmgr.h>",
	//"#include <vita2d.h>",
	"}"
})
@JTranscAddLibraries(target = "cpp", value = {
	//"jpeg",
	//"png",
	//"vita2d",
	//"z",
	"c",
	"SceKernel_stub",
	"SceKernel_stub",
	"SceDisplay_stub",
	"SceGxm_stub",
	"SceSysmodule_stub",
	"SceCtrl_stub",
	"ScePgf_stub",
	"SceTouch_stub",
	"SceCommonDialog_stub",
	//"z",
	//"png",
	//"jpeg",
	//"vita2d",
})
@JTranscAddFile(target = "cpp", prepend = "draw_include.c", process = true)
//@JTranscAddFlags(target = "cpp", value = {
//        "-Wl,-q"
//})
//-lc -lSceKernel_stub -lSceDisplay_stub -lSceGxm_stub -lSceCtrl_stub -lSceTouch_stub

public class ApiPsp2 extends Api {
	public ApiPsp2() {
		JTranscClock.impl = new JTranscClock.Impl(JTranscClock.impl) {
			@Override
			@JTranscMethodBody(target = "cpp", value = {
				"::sceKernelDelayThread((int32_t)(p0 * 1000.0));"
			})
			native public void sleep(double ms);
		};

		JTranscSyncIO.impl = new JTranscSyncIO.Impl(JTranscSyncIO.impl) {
			@Override
			@JTranscMethodBody(target = "cpp", value = {
				"char name[1024] = {0};",
				"auto str = N::istr3(p0);",
				"sprintf(name, \"%s\", str.c_str());",
				"std::vector<std::string> out;",
				"SceIoDirent dir;",
				"SceUID d = sceIoDopen(name);",
				"if (d >= 0) {",
				"	while (sceIoDread(d, &dir) > 0) {",
				"		out.push_back(std::string(dir.d_name));",
				"	}",
				"	sceIoDclose(d);",
				"}",
				"return N::strArray(out);"
			})
			native public String[] list(String file);

			@Override
			public JTranscSyncIO.ImplStream open(String path, int mode) throws FileNotFoundException {
				return super.open(path, mode);
			}

			@Override
			public String normalizePath(String path) {
				while (path.startsWith("/")) path = path.substring(1);
				if (!path.contains(":")) path = "app0:/" + path;
				return path;
			}

			@Override
			public boolean isAbsolute(String path) {
				return path.contains(":");
			}

			//@Override
			//@JTranscMethodBody(target = "cpp", value = {
			//	"struct stat stat_buf;",
			//	"char name[1024] = {0};",
			//	"::strcpy(name, N::istr3(p0).c_str());",
			//	"int rc = ::stat(name, &stat_buf);",
			//	"return (rc == 0) ? stat_buf.st_size : -1;",
			//})
			//public long getLength(String path) {
			//	return 0L;
			//}
		};
		//JTranscSyncIO.impl.setCwd("app0:/");
		JTranscSyncIO.impl.setCwd("/");
	}

	@Override
	public void init_video() {
		//Cpp.v_raw("::vita2d_init();");
		Cpp.v_raw("::init_video();");
	}

	@Override
	public void end_video() {
		Cpp.v_raw("::end_video();");
	}

	@Override
	public void clear_screen() {
		Cpp.v_raw("::clear_screen();");
	}

	@Override
	public void swap_buffers() {
		Cpp.v_raw("::swap_buffers();");
	}

	@Override
	public void draw_pixel(int x, int y, int color) {
		Cpp.v_raw("::draw_pixel(p0, p1, p2);");
	}

	@Override
	public void draw_rectangle(int x, int y, int w, int h, int color) {
		Cpp.v_raw("::draw_rectangle(p0, p1, p2, p3, p4);");
	}

	@Override
	public void font_draw_string(int x, int y, int color, String str) {
		Cpp.v_raw("int len = N::strLen(p3);");
		Cpp.v_raw("char *temp = (char*)malloc(len + 1);");
		Cpp.v_raw("memset(temp, 0, len + 1);");
		Cpp.v_raw("for (int n = 0; n < len; n++) temp[n] = N::strCharAt(p3, n);");
		Cpp.v_raw("::font_draw_string(p0, p1, p2, temp);");
		Cpp.v_raw("free((void*)temp);");
	}

	@Override
	protected void input_read_internal() {
		Cpp.v_raw("::input_read();");
		Cpp.v_raw("this->{% FIELD com.soywiz.psvita.ApiPsp2:pad_buttons %} = ::pad_buttons();");
	}

	@Override
	public void frame_end() {
		Cpp.v_raw("::frame_end();");
	}

	@Override
	public void sceDisplayWaitVblankStart() {
		Cpp.v_raw("::sceDisplayWaitVblankStart();");
	}

	@Override
	public void sceKernelExitProcess(int value) {
		Cpp.v_raw("::sceKernelExitProcess(p0);");
	}

	/*
	@JTranscMethodBody(target = "cpp", value = """
		char c_name[1024] = {0};
		char c_mode[1024] = {0};

		::strcpy(c_name, N::istr3(p0).c_str());
		::strcpy(c_mode, N::istr3(p1).c_str());

		return (int64_t)(void *)::fopen(c_name, c_mode);
	""")
	external fun fopen(str: String, mode: String): Long

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

	const val SEEK_SET                = 0
	const val SEEK_CUR                = 1
	const val SEEK_END                = 2
	*/
}