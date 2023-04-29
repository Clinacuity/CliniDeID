arguments="$*"

$JH/bin/java -Xmx28g -cp CliniDeIDComplete.jar com.clinacuity.deid.mains.DeidPipeline $arguments
