@echo off
if not exist out call "%~dp0build.bat"
java -cp out expensetracker.Main %*
