# This image is based off the latest Jenkins LTS
FROM jenkins:latest

USER root

RUN install-plugins.sh antisamy-markup-formatter matrix-auth pipeline-model-definition blueocean:$BLUEOCEAN_VERSION

USER jenkins
