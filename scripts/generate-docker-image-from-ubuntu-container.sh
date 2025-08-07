#!/bin/bash

docker run -it --name my_ubuntu -v /var/run/docker.sock:/var/run/docker.sock ubuntu:22.04

apt update && apt install -y docker.io && apt install -y git && apt install curl && apt install unzip && apt install zip

# SDKMAN! requirements


#Install sdkman!
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 21.0.2-graalce && sdk use java 21.0.2-graalce && sdk install gradle 8.7

apt install build-essential zlib1g-dev -y