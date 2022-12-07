#!/bin/bash
#this uses the zip made by makeDeployGenericZip.sh
#zip will contain:
#CliniDeIDComplete.jar
#runCliniDeID.bat/.sh, makeEnvironment, etc.
#COPYRIGHT notice
#classes/app*
#copy of data folder
#crt is in data, requirements.txt for python is in data/rnn

platform=$1

if [[ -z $platform ]]; then
	echo "Syntax is $0 (Windows|Mac|CentOs|Ubuntu|RedHat)"
	exit 1
elif [[ $platform == "Windows" ]]; then
	DIR="win"
elif [[ $platform == "Win" ]]; then
	platform="Windows"
	DIR="win"
elif [[ $platform == "Mac" ]]; then
	DIR="mac"
elif [[ $platform == "CentOs" ]]; then
	DIR="centOs"
elif [[ $platform == "Ubuntu" ]]; then
	DIR="ubuntu"
elif [[ $platform == "RedHat" ]]; then
	DIR="redhat"
else
	echo "Syntax is $0 (Windows|Mac|CentOs|Ubuntu|RedHat)"
	exit 1
fi

#to prevent updating existing zip:
if [[ -f CliniDeID-$platform.zip ]]; then
	rm CliniDeID-$platform.zip
fi

if [[ ! -f CliniDeID.zip ]]; then
	echo "Must create generic zip first"
	exit 1
fi

if [[ ! -d "$DEID_DIR" ]]; then
	echo "DEID_DIR must be set first"
	exit 1
fi

#create temp directory to make it easier to keep the zip file's directory structure simple
rm -r tempPlatform > /dev/null 2>&1
mkdir tempPlatform
cd tempPlatform

cp ../deploy/* .  > /dev/null 2>&1
cp ../deploy/$DIR/* .  > /dev/null 2>&1
cp ../CliniDeID.zip ./CliniDeID-$platform.zip

#q for quiet, X to ignore funny Mac files, r for recursive, m to move the files
zip -murq -X CliniDeID-$platform.zip * 
mv CliniDeID-$platform.zip ..
cd ..
rmdir tempPlatform

#tar with the --preserve-permissions for unix
#echo | sudo chmod -E CliniDeID.zip 
#sudo xattr -rc CliniDeID.zip 