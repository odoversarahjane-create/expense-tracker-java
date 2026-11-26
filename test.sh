#!/bin/sh
# Build the app, compile the JUnit 5 tests against the console launcher jar, and run them.
JUNIT_JAR=lib/junit-platform-console-standalone-1.10.2.jar
if [ ! -f "$JUNIT_JAR" ]; then
    echo "Missing $JUNIT_JAR"
    echo "Download it from https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/"
    exit 1
fi
./build.sh || exit 1
mkdir -p out/test-classes
javac -encoding UTF-8 -d out/test-classes -cp "out:$JUNIT_JAR" $(find test -name "*.java") || exit 1
java -jar "$JUNIT_JAR" execute --class-path "out:out/test-classes" --select-class expensetracker.ExpenseTrackerTest --details-theme=ascii --disable-banner
