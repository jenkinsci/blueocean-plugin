FROM jenkinsci/blueocean

# Security must be enabled to enable HTML rendering of the system message.
# Sacrifice to the Demo Gods.
ENV ADMIN_USERNAME admin
ENV ADMIN_PASSWORD admin

COPY blueocean-demo.png /usr/share/jenkins/ref/userContent/blueocean-demo.png
COPY *.groovy  /usr/share/jenkins/ref/init.groovy.d/
