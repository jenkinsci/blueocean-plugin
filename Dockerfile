# Build a Docker image bundling BlueOcean plugins and dependencies

FROM jenkinsci/jenkins:2.17
ENV BLUEOCEAN_VERSION 1.0-alpha-6

USER root
COPY download-plugins.sh /usr/local/bin
RUN /usr/local/bin/download-plugins.sh

USER jenkins
