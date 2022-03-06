#!/bin/bash
flag=`ps -ef | grep TG_LIVE_CHAT |grep -v grep|wc -l`

if [ ${flag} == 0 ]
        then
                echo "TG_LIVE_CHAT_2.0 is Running ..."
        else
                echo "TG_LIVE_CHAT_2.0 is Not Running ..."
fi

