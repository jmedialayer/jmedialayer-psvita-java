@echo off
SET PATH=%PATH%;C:\dev\psvita\bin
arm-vita-eabi-c++.exe -Wl,-q -O0 -std=c++0x build/jtransc-cpp/program.cpp -lc -lSceKernel_stub -lSceDisplay_stub -lSceGxm_stub -lSceCtrl_stub -lSceTouch_stub
REM arm-vita-eabi-c++.exe -Wl,-q -O3 -std=c++0x program2.cpp -lc -lSceKernel_stub -lSceDisplay_stub -lSceGxm_stub -lSceCtrl_stub -lSceTouch_stub
REM arm-vita-eabi-strip a.out
del /Q a.velf eboot.bin param.sfo output.vpk
vita-elf-create build/jtransc-cpp/a.out a.velf
vita-make-fself a.velf eboot.bin
vita-mksfoex -s TITLE_ID=XERP00002 "hellojavavita" param.sfo
vita-pack-vpk -s param.sfo -b eboot.bin
