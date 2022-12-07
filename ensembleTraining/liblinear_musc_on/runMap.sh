#inFile tFile mFile cFile thS
#inst template mapFile count?file threshold?
#cFile is both read and written?
#libHome?
#				  inFile(TrainFV) tFile(template)			mapFile 			cFile 			ths
java -Xmx20G CRFToSVMMap2 ../outputs/crf-FV ../template_crf_deid_musc crfMapUnified1 crfCountUnified1 0
