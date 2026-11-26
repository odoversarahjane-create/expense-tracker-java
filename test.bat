@echo off
rem Builds the app, compiles the JUnit 5 tests against the console launcher jar, and runs them.
set JUNIT_JAR=lib\junit-platform-console-standalone-1.10.2.jar
if not exist %JUNIT_JAR% (
    echo Missing %JUNIT_JAR%
    echo Download it from https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/
    exit /b 1
)
call "%~dp0build.bat" || exit /b 1
if not exist out\test-classes mkdir out\test-classes
dir /s /b test\*.java > test-sources.txt
javac -encoding UTF-8 -d out\test-classes -cp "out;%JUNIT_JAR%" @test-sources.txt
set TEST_BUILD_RESULT=%errorlevel%
del test-sources.txt
if not "%TEST_BUILD_RESULT%"=="0" (
    echo Test compilation FAILED.
    exit /b %TEST_BUILD_RESULT%
)
java -jar %JUNIT_JAR% execute --class-path "out;out\test-classes" --select-class expensetracker.ExpenseTrackerTest --details-theme=ascii --disable-banner
exit /b %errorlevel%
