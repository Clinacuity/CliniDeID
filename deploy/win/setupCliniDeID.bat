@echo off
mkdir openJdk17 2>nul
mkdir python3 2>nul
mkdir log 2>nul
::powershell -NoLogo -ExecutionPolicy RemoteSigned -file downloadAndUnzip.ps1
::del openjdk-17.0.1_windows-x64_bin.zip
::del python3WithRnn.zip
move openJdk17\jdk-17.0.1 .

rmdir .\openJdk17 > nul

SET JH=jdk-17.0.1
echo "Installing certificate for Open JDK"
%JH%\bin\keytool -import -trustcacerts -keystore %JH%\lib\security\cacerts -storepass changeit -noprompt -alias clinacuity -file data\clinacuitycom.crt
