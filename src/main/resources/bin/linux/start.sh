#!/bin/bash
SCHED_HOME=/usr/local/tigen/live_chat
var=$(ps -ef | grep ${SCHED_HOME})
sched_min_heap=128M
sched_max_heap=512M
LOG_CONFIG=${SCHED_HOME}/config
JAVA_OPT=-Xms${sched_min_heap}" -Xmx"${sched_max_heap}
IFS=' ' read -r -a array <<< $var
i=0
flag=0

while [ $i -lt ${#array[@]} ]
do
        if [ ${array[${i}]} == "java" ]
        then
                flag=1
                index=$(($i-6))
                pid=${array[${index}]}
                echo "process id : ${pid}"
        fi
        i=$(($i+1))
done

if [ ${flag} == 1 ]
        then
                echo "TG_LIVE_CHAT_2.0 is Already Started..."
        else
				nohup java -DTG_LIVE_CHAT $JAVA_OPT -jar TG_LIVE_CHAT_2.0.jar  --spring.config.name=application --logging.config=file:${LOG_CONFIG}/logback-spring.xml &
fi

