@echo off
title TG_LIVE_CHAT_2.0 STOP
pushd %~dp0
tasklist /FI "WINDOWTITLE eq TG_LIVE_CHAT_2.0"
taskkill /FI "WINDOWTITLE eq TG_LIVE_CHAT_2.0"
pause > NUL
