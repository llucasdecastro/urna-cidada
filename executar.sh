#!/usr/bin/env sh
# Compila e executa o Urna Cidadã sem precisar do Maven (só o JDK 17+).
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -Dfile.encoding=UTF-8 -cp out br.unijorge.urnacidada.App "$@"
