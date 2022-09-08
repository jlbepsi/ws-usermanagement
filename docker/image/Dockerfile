# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="jlb.epsi@gmail.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# The application's jar file
ARG JAR_FILE=target/ws-usermanagement.jar

# Create keys directory
RUN mkdir keys

# Add the application's jar to the container
COPY ${JAR_FILE} usermanagement.jar
# Add dependencies
COPY target/application.properties application.properties
COPY target/log4j2-spring.xml log4j2-spring.xml
COPY target/keys/private_key.der keys/private_key.der
COPY target/keys/public_key.der keys/public_key.der

# Run the jar file 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usermanagement.jar"]
