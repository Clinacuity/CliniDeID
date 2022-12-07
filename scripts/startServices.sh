#!/bin/bash
if [[ -d "$DEID_DIR" ]]
then
	#taggerHome=$DEID_DIR/data/ensemble/tagger-master_on_socket
	#$taggerHome/taggerSocket.py -m $taggerHome/models/${modelType}_deid_rnn &
	#echo "$!" >$DEID_DIR/taggerPid.txt

#	wapitiHome=$DEID_DIR/data/ensemble/wapiti-1.5.0_musc_on_socket
#    $wapitiHome/wapiti.OSX label -p -m $wapitiHome"/deid_model_$modelType" "--port" "4445" "none.in.txt" "none.out.txt"  > $DEID_DIR/log/wapiti.log 2>&1 &
# 	echo "$!" >$DEID_DIR/wapitiPid.txt
	cd $DEID_DIR/data/rnn
	mkdir -p $DEID_DIR/target/log
	startRnn.sh $DEID_DIR/target/data $DEID_DIR/target/data/models/rnn/rnn-Unified-Scheme2.h5 4444 #unified1-Dict.h5 4444
# 	echo "$!" >$DEID_DIR/rnnPid.txt	#not working?
#	startRnn.sh > $DEID_DIR/log/rnn.log 2>&1 &

else
	echo "Environmental variable DEID_DIR should be set to home directory of deid"
fi