@echo off
title TG_LIVE_CHAT_2.0 STATUS
pushd %~dp0
tasklist /FI "WINDOWTITLE eq TG_LIVE_CHAT"
pause > NUL
