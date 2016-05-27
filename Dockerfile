FROM jenkinsci/jenkins:2.6

COPY blueocean-commons/target/blueocean-commons.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-dashboard/target/blueocean-dashboard.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-plugin/target/blueocean-plugin.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-rest/target/blueocean-rest.hpi /usr/share/jenkins/ref/plugins/
COPY blueocean-web/target/blueocean-web.hpi /usr/share/jenkins/ref/plugins/

USER root
#TODO find some way to generate dependencies list
RUN cd /usr/share/jenkins/ref/plugins; \
	curl -LO http://updates.jenkins-ci.org/latest/metrics.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/variant.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/jackson2-api.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/sse-gateway.hpi ; \
	curl -LO http://updates.jenkins-ci.org/latest/git.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/github-branch-source.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/scm-api.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/token-macro.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/mailer.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-cps.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-job.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-api.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-step-api.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-support.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/workflow-multibranch.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/branch-api.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/favorite.hpi;  \
	curl -LO http://updates.jenkins-ci.org/latest/git-client.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/matrix-project.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/ssh-credentials.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/structs.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/durable-task.hpi; \ 
	curl -LO http://updates.jenkins-ci.org/latest/github.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/github-api.hpi; \ 
	curl -LO http://updates.jenkins-ci.org/latest/workflow-scm-step.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/jquery-detached.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/ace-editor.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/script-security.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/cloudbees-folder.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/script-security.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/junit.hpi; \
	curl -LO http://updates.jenkins-ci.org/latest/plain-credentials.hpi; 

RUN cd /usr/share/jenkins/ref/plugins; \
	curl -LO http://archives.jenkins-ci.org/plugins/credentials/2.0.4/credentials.hpi;

USER jenkins
