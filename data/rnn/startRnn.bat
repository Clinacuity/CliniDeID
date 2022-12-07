@echo off
echo "1=%1, model = %2, port is %3, dir is %cd%" >%1\..\log\startRnn.txt
cd python3
call python.exe RunDeid.py ..\%2 %3
