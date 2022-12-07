#!/bin/bash
#mira
trainMusc=y
if [ "$trainMusc" == "y" ]
then

	tmplateFile="../template_crf_deid_musc"
	trainFile="../outputs/featureVector-Dict"
	modelFile="../models/miraUnified-Dict"
	
	java -Xmx16G -cp trove-2.1.0.jar -jar mira.jar -t -f 0 -s 0.1 -n 30 -iob $tmplateFile $trainFile $modelFile
	
fi	


compileM=n
if [ "$compileM" == "y" ]
then
	javac -cp trove-2.1.0.jar:cplex.jar edu/lium/mira/*.java
	jar xf trove-2.1.0.jar gnu
	jar cfm mira.jar manifest.txt edu/ LICENSE LICENSE.trove gnu/
	rm -rf gnu/
fi

