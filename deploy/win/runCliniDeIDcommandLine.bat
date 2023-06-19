@setlocal enableextensions enabledelayedexpansion
java -Xmx28g -cp CliniDeIDComplete.jar com.clinacuity.deid.mains.DeidPipeline %*
