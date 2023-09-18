#!/bin/sh

MAIN_CLASS=/app/tailoringexpert-exec.jar
APP_CONFIG=file:///app/config/
LOADER_CONFIG=file:/app/lib/,file:$MAIN_CLASS
LOG_CONFIG=file:///app/config/log4j2.xml

# ***********************************************
# ***********************************************

ARGS="-Dloader.path=${LOADER_CONFIG} -Dspring.config.additional-location=${APP_CONFIG} -Dlog4j2.configurationFile=${LOG_CONFIG}"

exec $JAVA_HOME/bin/java $ARGS -jar $MAIN_CLASS
