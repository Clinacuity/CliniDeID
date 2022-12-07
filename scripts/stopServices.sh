#!/bin/bash

if [[ -d "$DEID_DIR" ]]
then
	if [[ -f "$DEID_DIR/rnnPid.txt" ]]
	then
		kill -9 `cat $DEID_DIR/rnnPid.txt` > /dev/null 2>&1
		rm $DEID_DIR/rnnPid.txt
	else
		#echo "No rnnPid.txt file found"
		cd $DEID_DIR/scripts
		java StopRnn
	fi
else
	echo "Environmental variable DEID_DIR should be set to home directory of deid"
fi