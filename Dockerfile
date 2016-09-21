FROM jenkinsci/jenkins:latest

USER root

# See JENKINS-34035 - disable upgrade wizard
RUN echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state  && \
    echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion

COPY blueocean/target/plugins /usr/share/jenkins/ref/plugins/

# Force use of locally built blueocean plugin
RUN for f in /usr/share/jenkins/ref/plugins/blueocean-*.hpi; do mv "$f" "$f.override"; done

USER jenkins
