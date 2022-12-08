#!/bin/bash
#zip will contain:
#CliniDeIDComplete.jar
#COPYRIGHT notice
#classes/app*
#copy of data folder
#crt is in data, requirements.txt for python is in data/rnn
#no platform specific scripts
#need change version number before running `mvn clean package`

if [[ -f CliniDeID.zip ]]; then
	rm CliniDeID.zip
fi

#if [[ ! -d "$DEID_DIR" ]]; then
#	echo "DEID_DIR must be set first"
#	exit 1
#fi

#so that zip doesn't get target folder
cd target # $DEID_DIR/clinideid-legacy-app/target

if [[ -f clinideid-legacy-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar ]]; then
	mv clinideid-legacy-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar CliniDeIDComplete.jar
fi

#q for quiet, X to ignore funny Mac files, r for recursive
#tar with the --preserve-permissions for unix
#zip -q -X ../CliniDeID.zip classes classes/app*
zip -qr -X ../CliniDeID.zip CliniDeIDComplete.jar classes classes/app* data/

#echo | sudo chmod -E CliniDeID.zip 
#sudo xattr -rc CliniDeID.zip 
