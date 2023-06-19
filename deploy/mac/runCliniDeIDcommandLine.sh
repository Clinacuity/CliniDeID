arguments="$*"

java -Xdock:icon=classes/gui/CliniDeID-app.icns -Xmx28g -cp CliniDeIDComplete.jar com.clinacuity.deid.mains.DeidPipeline $arguments
