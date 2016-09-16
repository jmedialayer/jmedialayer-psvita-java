package com.soywiz.psvita;

import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscAddLibraries;
import com.jtransc.annotation.JTranscNativeClass;
import com.jtransc.annotation.JTranscNativeName;

@JTranscNativeName("::")
@JTranscAddHeader(target = "cpp", value = {
	"extern \"C\" {",
	"#include <vita2d.h>",
	"}"
})
@JTranscAddLibraries(target = "cpp", value = {
	"jpeg",
	"png",
	"vita2d",
	"z",
})
public class Vita2D {
	native static public void memset(int ptr, int value, int size);
	native static public int malloc(int size);
	native static public int free(int ptr);
	native static public int sizeof(String name);

	static public int allocPad(String name) {
		return malloc(sizeof("SceCtrlData"));
	}

	native static public void vita2d_init();
	native static public void vita2d_fini();
	native static public void vita2d_set_clear_color(int value);
	native static public vita2d_pgf vita2d_load_default_pgf();

	@JTranscNativeName("vita2d_pgf")
	static public class vita2d_pgf {
	}
}



//@JTranscNativeName("vita2d_pgf")
//public class vita2d_pgf {
//}

