#
# Before building this Dockerfile, BlueOcean needs to be built locally using Maven
# You can build everything needed and this Dockerfile by invoking `bin/build-in-docker.sh -m`
#

# Should be kept in sync with jenkins.properties of pom.xml
FROM jenkinsci/jenkins:2.8

USER root

COPY blueocean/target/plugins /usr/share/jenkins/ref/plugins/

RUN for f in /usr/share/jenkins/ref/plugins/*.hpi; do mv "$f" "${f%%hpi}jpi"; done
RUN install-plugins.sh antisamy-markup-formatter matrix-auth # for security, you know

# Force use of locally built blueocean plugin
RUN for f in /usr/share/jenkins/ref/plugins/blueocean-*.jpi; do mv "$f" "$f.override"; done

USER jenkins
