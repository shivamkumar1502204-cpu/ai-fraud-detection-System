@echo off
echo ===================================================
echo  Compiling AI Fraud Detection System...
echo ===================================================
if not exist "target\classes" mkdir "target\classes"
powershell -Command "Get-ChildItem -Path src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName | Out-File -FilePath sources.txt -Encoding ascii"
javac -cp "lib/*;src/main/java" -d target/classes "@sources.txt"
if exist sources.txt del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation complete. Classes output to target\classes\
) else (
    echo [ERROR] Compilation failed.
)
pause
