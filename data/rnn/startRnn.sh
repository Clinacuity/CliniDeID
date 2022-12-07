#! /bin/bash
echo `pwd` > $1/../log/startRnnPwd.txt
echo "1=$1, 2=$2, 3=$3" >> $1/../log/startRnnPwd.txt
if [[ -f "PythonRnnError.txt" ]] ; then
	rm PythonRnnError.txt
fi

if [[ -d "deid-rnn-env" ]] ; then
	source deid-rnn-env/bin/activate
	bin=deid-rnn-env/bin
	echo "first" >> $1/../log/startRnnPwd.txt
elif [[ -d "$HOME/deid-rnn-env" ]] ; then
	source $HOME/deid-rnn-env/bin/activate
	bin=$HOME/deid-rnn-env/bin
	echo "second" >> $1/../log/startRnnPwd.txt
elif [[ -d "target/deid-rnn-env" ]] ; then
	source target/deid-rnn-env/bin/activate
	bin=target/deid-rnn-env/bin
	echo "third" >> $1/../log/startRnnPwd.txt
else
	echo "Couldn't find python environment" > PythonRnnError.txt
	exit -1
fi
echo `ls -l $1/rnn/RunDeid.py` >>$1/../log/startRnnPwd.txt

$bin/python3 $1/rnn/RunDeid.py $2 $3 > $1/../log/rnn.log 2>&1 &
echo "$!" > rnnPid.txt
exit 0
