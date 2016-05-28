FROM jenkinsci/jenkins:2.6

COPY blueocean-commons/target/blueocean-commons.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-dashboard/target/blueocean-dashboard.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-plugin/target/blueocean-plugin.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-rest/target/blueocean-rest.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-web/target/blueocean-web.hpi /usr/share/jenkins/ref/plugins/

USER root

RUN cd /usr/share/jenkins/ref/plugins/; \
	install-plugins.sh blueocean-commons \
                       blueocean-dashboard \
                       blueocean-plugin \
                       blueocean-rest \
                       blueocean-web

USER jenkins
