#!/bin/bash
chmod 700 data/rnn/*.sh
mkdir -p log

echo "Uncompressing Open JDK 17"
tar -zxf openjdk-17.0.1_macos-x64_bin.tar.gz
status=$?
if [ $status -ne 0 ]; then
	echo "Failed to uncompress Open JDK 17"
	exit $status
fi
rm openjdk-17.0.1_macos-x64_bin.tar.gz

echo "Installing certificate for Open JDK"
. ./findJava.sh
sudo $JH/bin/keytool -import -trustcacerts -keystore $JH/lib/security/cacerts -storepass changeit -noprompt -alias clinacuity -file data/clinacuitycom.crt

echo "Downloading Python 3.10"
curl -s https://www.python.org/ftp/python/3.10.9/python-3.10.9-macos11.pkg  -o python-3.10.9-macos11.pkg
echo "Installing Python 3.10"
sudo installer -pkg python-3.10.9-macos11.pkg -target /
rm python-3.10.9-macos11.pkg

echo "Preparing Python environment"
/usr/local/bin/python3.10 -m venv deid-rnn-env
source deid-rnn-env/bin/activate
deid-rnn-env/bin/python3.10 -m pip install -q --upgrade pip
deid-rnn-env/bin/pip3.10 install -qr data/rnn/requirements.txt
deactivate
