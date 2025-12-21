#!/bin/bash

# Create libs directory
mkdir -p libs

# Download NightCore POM and Jar
echo "Downloading NightCore..."
curl -L -o libs/nightcore-2.7.15.pom https://github.com/vanes430/nightcore-folia/raw/master/pom.xml
curl -L -o libs/nightcore-2.7.15.jar https://github.com/vanes430/nightcore-folia/releases/download/latest/nightcore-2.7.15.jar

# Install to local maven repository
echo "Installing NightCore to local Maven repository..."
mvn install:install-file -Dfile=libs/nightcore-2.7.15.pom -DgroupId=su.nightexpress.nightcore -DartifactId=nightcore -Dversion=2.7.15 -Dpackaging=pom
mvn install:install-file -Dfile=libs/nightcore-2.7.15.jar -DgroupId=su.nightexpress.nightcore -DartifactId=main -Dversion=2.7.15 -Dpackaging=jar

# Build the project
echo "Building project..."
mvn clean
mvn install
