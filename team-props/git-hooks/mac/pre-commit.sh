#!/usr/bin/env bash
echo "Running static analysis..."

echo "Start running ktlint"
./gradlew ktlintFormat ktlintCheck --daemon
status1=$?
if [[ "$status1" = 0 ]] ; then
    echo "*******************************************************"
    echo "             Ktlint runs successfully                  "
    echo "*******************************************************"
else
    echo "*******************************************************"
    echo "                 Ktlint failed                         "
    echo "     Please fix the reported issues before committing. "
    echo "*******************************************************"
    exit status1
fi

echo "Start running detektCheck"
./gradlew detekt --daemon
status2=$?
if [[ "$status2" = 0 ]] ; then
    echo "*******************************************************"
    echo "             Detekt runs successfully                  "
    echo "*******************************************************"
else
    echo "*******************************************************"
    echo "                 Detekt failed                         "
    echo "   Please fix the reported issues before committing.   "
    echo "*******************************************************"
    exit status2
fi

echo "Start running unit test"
./gradlew testDebugUnitTest --daemon
status3=$?
if [[ "$status3" = 0 ]] ; then
    echo "*******************************************************"
    echo "                 UnitTest successfully                 "
    echo "*******************************************************"
else
    echo "*******************************************************"
    echo "                   UnitTest failed                     "
    echo "   Please fix the unit test issues before committing   "
    echo "*******************************************************"
    exit status3
fi

# add changed files after run auto format
git add .
