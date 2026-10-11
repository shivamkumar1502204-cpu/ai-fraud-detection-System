@echo off
echo ===================================================
echo  Running Rubric Automated Verification Tests...
echo ===================================================
java -cp "lib/*;target/classes;src/main/java" com.frauddetection.test.SystemVerificationTest
pause
