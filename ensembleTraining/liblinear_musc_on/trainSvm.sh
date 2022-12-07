name="-MuscHcuNoTime-New"

fv="../featureVectors/fvCrf-New"
mapFile="../outputs/svmMapUnified$name"
countFile="../outputs/svmCountUnified$name"
outFile="../outputs/svmOutUnified$name"
modelFile="../models/svmModelUnified$name"
template="../template_crf_deid_musc"
typesFile="../bioDeid_musc.txt"

#		  	inFile tFile	mapFile  cFile 	   ths
: > $mapFile
: > $countFile
#make sure they exist
java -Xmx24G CRFToSVMMap2 $fv $template $mapFile $countFile 0
#			 inFile  tFile 	 outFile  mFile  ifTrainS ifLibLinearS  ifIncS    lMapFile
: > $outFile
java  -Xmx24g CRFToSVMNew2 $fv $template $outFile $mapFile 0 1 0 $typesFile

train -w0 0.01 $outFile $modelFile
