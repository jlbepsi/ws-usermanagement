# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="jlb.epsi@gmail.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8081 available to the world outside this container
EXPOSE 8081

# The application's jar file
ARG JAR_FILE=target/ws-usermanagement.jar

# Create keys directory
RUN mkdir keys
# Create SSL key directory
RUN mkdir keystore

# Add the application's jar to the container
COPY ${JAR_FILE} usermanagement.jar
# Add dependencies
COPY target/application.properties application.properties
COPY target/keys/private_key.der keys/private_key.der
COPY target/keys/public_key.der keys/public_key.der
COPY target/keystore/ws_users.p12 keystore/ws_users.p12

# Run the jar file 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usermanagement.jar"]
