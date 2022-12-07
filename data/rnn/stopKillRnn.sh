#!/bin/bash
if [[ -f "rnnPid.txt" ]]
then
	kill -9 `cat rnnPid.txt` #> /dev/null 2>&1
	rm rnnPid.txt
else
	echo "No rnnPid.txt file found"
	echo "No rnnPid.txt file found" > stopKillOutput.txt
fi