#!/bin/bash
flag=`ps -ef | grep TGLMS_ENCODER |grep -v grep|wc -l`

if [ ${flag} == 0 ]
        then
                echo "Encoder is Already Stopped ..."
        else
                ps -ef | grep TGLMS_ENCODER |grep -v grep |awk '{print "kill -9 "$2}' | sh -x 
fi

if [ ${flag} == 0 ]
        then
                echo "Encoder is Stopped ..."
        else
                echo "Failed to Stop Encoder !!!"
fi

