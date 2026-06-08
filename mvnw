#!/usr/bin/env bash
MVNW_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_WRAPPER_JAR="$MVNW_DIR/.mvn/wrapper/maven-wrapper.jar"
exec java -Dmaven.multiModuleProjectDirectory="$MVNW_DIR" -cp "$MAVEN_WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
