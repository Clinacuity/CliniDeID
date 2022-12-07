#!/bin/bash

# Sets environment variables for the Machine ID and the Instance ID
#export MID=$(cat /sys/hypervisor/uuid)		#unused and uuid file does not exist anymore
export IID=$(ec2-metadata -i)

# This file is embedded in the production-level AMI, but we should determine a better way to manage
# environment properties without maintaining multiple files (like the singular application.yml file).
echo 'Current environment is: production'

jdk-12.0.2/bin/java \
  -Dserver.tomcat.max-http-header-size=100000 \
  -Xmx27g \
  -Dclinacuity.deid.instanceId="${IID}" \
  -Dclinacuity.deid.autoScaled=true \
  -Dspring.profiles.active="production" \
  -jar clinideid.jar
