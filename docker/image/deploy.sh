#!/bin/bash

scp docker/image/Dockerfile  root@192.168.100.7:/docker/configuration/ws_usermanagement/image/
scp docker/image/target/log4j2-spring.xml  root@192.168.100.7:/docker/configuration/ws_usermanagement/image/target/
scp build/libs/ws-usermanagement-0.9.5.jar root@192.168.100.7:/docker/configuration/ws_usermanagement/image/target/ws-usermanagement.jar
