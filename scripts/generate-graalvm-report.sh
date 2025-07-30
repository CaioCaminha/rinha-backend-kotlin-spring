#!/bin/bash

./gradlew clean bootJar

java -agentlib:native-image-agent=config-output-dir=target/native-image -jar build/libs/rinha-backend-kotlin-spring-native-0.0.1-SNAPSHOT.jar
