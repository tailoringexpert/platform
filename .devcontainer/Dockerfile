ARG USERNAME=developer
ARG USER_UID=1001
ARG USER_GID=1001

ARG MAVEN_JDK_VERSION=3.9.12-eclipse-temurin-21
FROM maven:${MAVEN_JDK_VERSION}

RUN \
	groupadd -g 1001 developer && \
	useradd -u 1001 -g 1001 -m -s /bin/bash developer && \
	passwd -d developer && \
    apt-get update && \
    apt-get install -y sudo  && \
    echo developer ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/developer && \
    chmod 0440 /etc/sudoers.d/developer
	
ARG NODE_VERSION=lts
RUN \
    curl -sL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - && \
    apt-get update -qqy && \
    apt-get install -qqy gcc g++ make nodejs && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/*
	
	
#USER 1000

