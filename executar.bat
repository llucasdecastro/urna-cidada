@echo off
REM Compila e executa o Urna Cidada sem precisar do Maven (so o JDK 17+).
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
del fontes.txt
java -Dfile.encoding=UTF-8 -cp out br.unijorge.urnacidada.App %*
