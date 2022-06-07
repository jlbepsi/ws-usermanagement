#!/bin/bash

# Build
./gradlew build

# Delete files from server
sh /docker/configuration/ws_usermanagement/image/clearTarget.sh

# Copy Dockerfile & makeWS to server
cp docker/image/Dockerfile  /docker/configuration/ws_usermanagement/image/
cp docker/image/makeWSDockerImage.sh  /docker/configuration/ws_usermanagement/image/

# Copy files to server
cp build/libs/ws-usermanagement-0.9.5.jar /docker/configuration/ws_usermanagement/image/target/ws-usermanagement.jar
cp docker/image/target/application.properties  /docker/configuration/ws_usermanagement/image/target/
cp docker/image/target/log4j2-spring.xml  /docker/configuration/ws_usermanagement/image/target/
cp docker/image/target/keys/private_key.der  /docker/configuration/ws_usermanagement/image/target/keys/
cp docker/image/target/keys/public_key.der   /docker/configuration/ws_usermanagement/image/target/keys/

# Rebuild docker image
sh /docker/configuration/ws_usermanagement/image/makeWSDockerImage.sh