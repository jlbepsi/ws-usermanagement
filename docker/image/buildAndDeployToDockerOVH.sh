#!/bin/bash

# Build
./gradlew build

echo "Build and Deploy to OVH Docker"

# Delete files from server
ssh -p 5102 root@176.31.233.13 /docker/configuration/ws_usermanagement/clearTarget.sh

# Copy Dockerfile to server
scp -P 5102 docker/image/Dockerfile  root@176.31.233.13:/docker/configuration/ws_usermanagement/

# Copy files to server
scp -P 5102 build/libs/ws-usermanagement-0.9.5.jar root@176.31.233.13:/docker/configuration/ws_usermanagement/target/ws-usermanagement.jar
scp -P 5102 docker/image/target/application.properties.ovh  root@176.31.233.13:/docker/configuration/ws_usermanagement/target/application.properties
scp -P 5102 docker/image/target/log4j2-spring.xml  root@176.31.233.13:/docker/configuration/ws_usermanagement/target/
scp -P 5102 docker/image/target/keys/private_key.der  root@176.31.233.13:/docker/configuration/ws_usermanagement/target/keys/
scp -P 5102 docker/image/target/keys/public_key.der   root@176.31.233.13:/docker/configuration/ws_usermanagement/target/keys/

# Rebuild docker image
ssh -p 5102 root@176.31.233.13 /docker/configuration/ws_usermanagement/makeWSDockerImage.sh