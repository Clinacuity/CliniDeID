if [[ ! -d "$DEID_DIR" ]]; then
	echo "DEID_DIR must be set first"
	exit 1
fi
cd $DEID_DIR/target
cp ../LICENSE.KEY .
java -Xmx28g -Dclinacuity.deid.baseUrl=https://devtest1.clinacuity.com/clinacuity/v1/clinideid/ \
 -jar clinideid-legacy-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar
rm LICENSE.KEY