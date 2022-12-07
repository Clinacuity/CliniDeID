chmod 700 data/rnn/*.sh
mkdir -p log

echo "Uncompressing Open JDK 17"
tar -zxf openjdk-17.0.1_linux-x64_bin.tar.gz
status=$?
if [ $status -ne 0 ]; then
	echo "Failed to uncompress Open JDK 17"
	exit $status
fi
rm openjdk-17.0.1_linux-x64_bin.tar.gz

echo "Installing certificate for Open JDK"
. ./findJava.sh
sudo $JH/bin/keytool -import -trustcacerts -keystore $JH/lib/security/cacerts -storepass changeit -noprompt -alias clinacuity -file data/clinacuitycom.crt

echo "Downloading and Building Python 3.8"
curl -s https://www.python.org/ftp/python/3.8.7/Python-3.8.7.tgz | tar -xz
sudo yum -y -q install openssl-devel libffi-devel sqlite-devel
cd Python-3.8.7
./configure -q --enable-loadable-sqlite-extensions
make -s
cd ..

echo "Preparing Python environment"
./Python-3.8.7/python -m venv deid-rnn-env
source deid-rnn-env/bin/activate
./deid-rnn-env/bin/python -m pip install -q --upgrade pip
./deid-rnn-env/bin/pip3.8 install -qr data/rnn/requirements.txt
deactivate
