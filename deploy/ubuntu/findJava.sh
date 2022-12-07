
#for this script to work from inside other scripts must be invoked with:
#   . ./findJava.sh
if [ -d "jdk-17.jdk" ]; then 
	export JH="jdk-17.jdk"
elif [ -d "jdk-17" ]; then 
	export JH="jdk-17"
elif [ -d "jdk-17.0.1.jdk" ]; then
	export JH="jdk-17.0.1.jdk"
elif [ -d "jdk-17.0.1" ]; then
	export JH="jdk-17.0.1"
elif [ -d "jdk-17.0.2.jdk" ]; then
	export JH="jdk-17.0.2.jdk"
elif [ -d "jdk-17.0.2" ]; then
	export JH="jdk-17.0.2"
elif [ -d $JAVA_HOME ]; then
	export JH=$JAVA_HOME
else
	temp=`ls |grep 'jdk-17' | tail -n1`
	export JH="$temp"
	if [ -z $JH ]; then
		echo "Could not locate jdk-17 folder."
		exit 1
	fi
fi
