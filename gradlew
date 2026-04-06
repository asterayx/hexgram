#!/bin/sh
# Gradle wrapper stub - downloads and runs Gradle
APP_NAME="Gradle"
CLASSPATH=gradle/wrapper/gradle-wrapper.jar
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
