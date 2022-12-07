#!/bin/bash

# This script runs when an auto-scaled instance is launched
# The script installs apache, the SSL mod, and java
# Then it downloads relevant files required for configuring the HTTPD and systemctl
#
# All accompanying files should be part of this directory and copied to & overwritten in S3 as part of
# a Jenkins CI job to ensure the S3 bucket consistently has the latest files
#

# Add python's install location to PATH, then install updates
echo "export PATH=\$PATH:/usr/local/bin" >>~/.bash_profile
source ~/.bash_profile
cd /home/ec2-user
sudo echo "My name is $(whoami)" >whoami.txt
sudo yum update -y

if [[ $DEPLOYMENT_ENV == "production" ]]; then
  deidDownloadPath='s3://clinacuity/deployments/deid-service'
else
  deidDownloadPath='s3://clinacuity/deployments/deid-service-test'
fi

# Install apache server
sudo yum install -y httpd mod_ssl openssl-devel sqlite-devel                        #may not need httpd and mod_ssl unless python/pip needs them
aws s3 cp $deidDownloadPath/clinacuity2019.prkey /etc/pki/tls/private/localhost.key #not needed
aws s3 cp $deidDownloadPath/clinacuity2019.cert /etc/pki/tls/certs/localhost.crt
aws s3 cp $deidDownloadPath/httpd.conf /etc/httpd/conf.d/ssl.conf #not needed

# Download and install java to custom directories; and add cert file to the JDK
# Any changes in the java version must be copied onto the `start.sh` file
wget https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_linux-x64_bin.tar.gz
tar -xvzf openjdk-12.0.2_linux-x64_bin.tar.gz
jdk-12.0.2/bin/keytool -import -alias "clinacuity" -file /etc/pki/tls/certs/localhost.crt -keystore jdk-12.0.2/lib/security/cacerts -storepass changeit -noprompt

# Download and build Python 5.3.5
# The installation has a bug in the test_socket test which causes it to hang; we can install after deleting tests
#TODO: consider having a zipped download of python already configured and having pip stuff installed

sudo yum groupinstall "Development Tools" -y
wget https://www.python.org/ftp/python/3.5.3/Python-3.5.3.tgz
tar -xvzf Python-3.5.3.tgz
cd Python-3.5.3
#aws s3 cp s3://clinacuity/deployments/deid-service/test_socket.py ./Lib/test/test_socket.py
rm Lib/test/*.py #needed b/c a test will fail preventing proper installation, also tests are slow
./configure --enable-optimizations --with-ssl --enable-loadable-sqlite-extensions
sudo make altinstall
cd ..

# Copies from S3 the JAR file, the running script, the models, the python env requirements, and the service (unit) files
aws s3 cp $deidDownloadPath/data.zip data.zip
aws s3 cp $deidDownloadPath/start-rnn.sh start-rnn.sh
aws s3 cp $deidDownloadPath/deid-rnn.service /etc/systemd/system/deid-rnn.service
aws s3 cp $deidDownloadPath/clinideid.jar clinideid.jar
aws s3 cp $deidDownloadPath/start.sh start.sh
aws s3 cp $deidDownloadPath/deid.service /etc/systemd/system/deid.service

# Get & install pip; install RNN requirements
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3.5 get-pip.py
unzip data.zip
pip3.5 install -r data/rnn/requirements.txt

# Update ownership and permissions of files
chown -R ec2-user:ec2-user /home/ec2-user/
chmod 755 start-rnn.sh
chmod 755 start.sh

# Start all processes -- HTTPD goes last since the server should not be responsive until all other services are running
systemctl start deid-rnn
systemctl enable deid-rnn
systemctl start deid
systemctl enable deid
systemctl start httpd #httpd not needed
systemctl enable httpd

# A simple verification of completion
echo "$(whoami) is done" >done.txt
