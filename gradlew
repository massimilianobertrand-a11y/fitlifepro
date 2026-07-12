#!/bin/sh
#
# Gradle wrapper startup script for Linux/macOS
#
APP_HOME=$( cd "${0%[/\\]*}" > /dev/null && pwd -P ) || exit
APP_HOME=$( cd "$APP_HOME" > /dev/null && pwd -P ) || exit

exec "${JAVA_HOME:-/usr}/bin/java" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
