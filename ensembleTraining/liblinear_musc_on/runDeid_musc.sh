#!/bin/bash
#liblinear
tmplateFile_musc="../template_crf_deid_musc"
LFile_musc="bioDeid_musc.txt"
#please check if the phi types are corresponding to new corpus

wMap="feature index map file"
cMap="feature count map file"

libTr="../outputs/crf-FV" #"feature vector train file (used for crf or mira) path"
oLibTr="../models/svmFvSmall" #"new fv file for svm"
libModel="../models/svmSmall" #"svm model file"

run=y
if [ "$run" == "y" ]
then

	mapInitTr=y
	if [ "$mapInitTr" == "y" ]
	then	
		echo "init cnt map"
		: > $wMap
		: > $cMap
		cutoff=0
		
		java -cp $libHome -Xmx13g CRFToSVMMap2 $libTr $tmplateFile_musc $wMap $cMap $cutoff
		java -cp $libHome -Xmx13g CRFToSVMNew2 $libTr $tmplateFile_musc $oLibTr $wMap 0 1 0 $LFile_musc
	fi

	train=y
	if [ "$train" == "y" ]
	then
		$libHome/train -w0 0.01 $oLibTr $libModel
	fi
	
fi
