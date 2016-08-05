FROM jenkinsci/jenkins:latest

USER root

RUN mkdir /usr/share/jenkins/ref/plugins/ && cd /usr/share/jenkins/ref/plugins/ && install-plugins.sh blueocean

RUN for f in /usr/share/jenkins/ref/plugins/blueocean*.jpi; do mv "$f" "$f.override"; done

# See JENKINS-34035 - disable upgrade wizard
RUN echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state  && \
    echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion

USER jenkins
