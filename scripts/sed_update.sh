`sed -i 's/VERSION_NUMBER/!{q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/src/main/resources/gui/DeidRunner.fxml`
RESULT1=$?
`sed -i 's/VERSION_NUMBER/{!q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/src/main/java/com/clinacuity/deid/mains/DeidPipeline.java`
RESULT2=$?

#update version in Readme files
`sed -i 's/VERSION_NUMBER/{!q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/deploy/mac/Readme.txt`
RESULT3=$?
`sed -i 's/VERSION_NUMBER/{!q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/deploy/ubuntu/Readme.txt`
RESULT4=$?
`sed -i 's/VERSION_NUMBER/{!q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/deploy/centOs/Readme.txt`
RESULT5=$?
`sed -i 's/VERSION_NUMBER/{!q100}; s/VERSION_NUMBER/'$VERSION_NUMBER'/g' java-app/deploy/win/Readme.txt`
RESULT6=$?

if [[ RESULT1 -eq 0 && RESULT2 -eq 0 && RESULT3 -eq 0 && RESULT4 -eq 0 && RESULT5 -eq 0 && RESULT6 -eq 0 ]]
then
	exit 0
else
	exit 1
fi
