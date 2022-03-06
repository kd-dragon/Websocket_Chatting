@echo off
title TG_LIVE_CHAT
pushd %~dp0
setLocal EnableDelayedExpansion
set SCHED_HOME=D:\springBoot\TG_LIVE_CHAT_2.0
set JAVA_HOME=D:\IDE\Java\jdk1.8.0_261
set LOG_CONFIG=%SCHED_HOME%\config
set sched_min_heap=128M
set sched_max_heap=512M

set JAVA_OPT=-Xms%sched_min_heap%
set JAVA_OPT=%JAVA_OPT% -Xmx%sched_max_heap%
%JAVA_HOME%\jre\bin\java %JAVA_OPT% -jar TG_LIVE_CHAT_2.0.jar --spring.config.name=application --logging.config=file:%LOG_CONFIG%\logback-spring.xml
pause > NUL
