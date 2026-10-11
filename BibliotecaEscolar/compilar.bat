@echo off
cd /d "%~dp0"
if not exist target\classes mkdir target\classes
dir /s /b src\main\java\*.java > fuentes.txt
javac -encoding UTF-8 --release 17 -d target\classes @fuentes.txt
if errorlevel 1 goto fin
jar cfm BibliotecaEscolar.jar manifest.mf -C target\classes .
:fin
pause
