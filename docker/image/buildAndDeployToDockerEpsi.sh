#!/bin/bash

# Build
./gradlew build

# Delete files from server
ssh root@192.168.100.7 /docker/configuration/ws_usermanagement/image/clearTarget.sh

# Copy files to server
scp docker/image/Dockerfile  root@192.168.100.7:/docker/configuration/ws_usermanagement/image/
scp docker/image/target/log4j2-spring.xml  root@192.168.100.7:/docker/configuration/ws_usermanagement/image/target/
scp build/libs/ws-usermanagement-0.9.5.jar root@192.168.100.7:/docker/configuration/ws_usermanagement/image/target/ws-usermanagement.jar

# Rebuild docker image
ssh root@192.168.100.7 /docker/configuration/ws_usermanagement/image/makeWSDockerImage.sh