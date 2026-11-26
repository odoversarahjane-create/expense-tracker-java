@echo off
rem Compiles the application (src only) into .\out  - requires JDK 11 or newer.
rem JUnit is NOT needed to build or run the app; see test.bat for the tests.
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
set BUILD_RESULT=%errorlevel%
del sources.txt
if not "%BUILD_RESULT%"=="0" (
    echo Build FAILED.
    exit /b %BUILD_RESULT%
)
echo Build finished.
