java -Xmx28g -Dclinacuity.deid.baseUrl=https://devtest1.clinacuity.com/clinacuity/v1/clinideid/ \
-cp deid-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.clinacuity.deid.mains.DeidPipeline  $*