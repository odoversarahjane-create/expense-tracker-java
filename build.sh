#!/bin/sh
# Compile the application (src only) into ./out  (requires JDK 11 or newer).
# JUnit is NOT needed to build or run the app; see test.sh for the tests.
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") && echo "Build OK"
