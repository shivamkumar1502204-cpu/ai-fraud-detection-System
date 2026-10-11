#!/bin/bash
echo "==================================================="
echo " Compiling AI Fraud Detection System..."
echo "==================================================="
mkdir -p target/classes
find src/main/java -name "*.java" > sources.txt
javac -cp "lib/*:src/main/java" -d target/classes @sources.txt
rm -f sources.txt
echo "[SUCCESS] Compilation complete."
