#!/bin/sh
# Build (if needed) and start the Expense Tracker
[ -d out ] || ./build.sh
java -cp out expensetracker.Main "$@"
