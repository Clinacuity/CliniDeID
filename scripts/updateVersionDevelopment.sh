#!/bin/bash
#parameter value uses as new version number in GUI display, DB values, and Readme.txt files and changes log4j2.xml to ERROR level
#if no parameter given then it reverts to VERSION_NUMBER, and reverts log4j2.xml to DEBUG
#should be run from deid/java-app/clinideid-legacy folder

VN=$1
if [[ -n $1 ]] 
then
	echo "changing version number to $VN"
	perl -pi -w -e "s/VERSION_NUMBER/Version: $VN/g;" src/main/resources/gui/DeidRunner.fxml
	perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" src/main/java/com/clinacuity/deid/mains/DeidPipeline.java

    perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" deploy/mac/Readme.txt
    perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" deploy/ubuntu/Readme.txt
    perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" deploy/centOs/Readme.txt
    perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" deploy/win/Readme.txt
    perl -pi -w -e "s/VERSION_NUMBER/$VN/g;" deploy/redhat/Readme.txt

    perl -pi -w -e 's/"DEBUG"/"ERROR"/g;' src/main/resources/log4j2.xml
else
	echo "Reverting version number to VERSION_NUMBER"
	 			#<Label text="Version: 1.6.1" styleClass="text-blue" fx:id="VersionLabel" alignment="CENTER_RIGHT"/>

	perl -pi -w -e "s/text=\"\.*?\"(.*)fx:id=\"VersionLabel\"/text=\"VERSION_NUMBER\"\$1fx:id=\"VersionLabel\"/;"  src/main/resources/gui/DeidRunner.fxml
	perl -pi -w -e "s/public static final String VERSION\s+=\s+\"\S+\"/public static final String VERSION = \"VERSION_NUMBER\"/;" src/main/java/com/clinacuity/deid/mains/DeidPipeline.java

    perl -pi -w -e "s/Version \d+(\.\d+)?(\.\d+)?/Version VERSION_NUMBER/g;" deploy/mac/Readme.txt
    perl -pi -w -e "s/Version \d+(\.\d+)?(\.\d+)?/Version VERSION_NUMBER/g;" deploy/ubuntu/Readme.txt
    perl -pi -w -e "s/Version \d+(\.\d+)?(\.\d+)?/Version VERSION_NUMBER/g;" deploy/centOs/Readme.txt
    perl -pi -w -e "s/Version \d+(\.\d+)?(\.\d+)?/Version VERSION_NUMBER/g;" deploy/win/Readme.txt
    perl -pi -w -e "s/Version \d+(\.\d+)?(\.\d+)?/Version VERSION_NUMBER/g;" deploy/redhat/Readme.txt

    perl -pi -w -e 's/"ERROR"/"DEBUG"/g;' src/main/resources/log4j2.xml

fi