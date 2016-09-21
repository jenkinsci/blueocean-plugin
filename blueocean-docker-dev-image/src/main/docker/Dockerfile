FROM jenkinsci/jenkins:2.17

USER root

# See JENKINS-34035 - disable upgrade wizard
RUN echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state  && \
    echo -n 2.0 > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion

COPY plugins /usr/share/jenkins/ref/plugins/

# Force use of latest blueocean plugin, until this one is published and users can rely on update center for updates
RUN for f in /usr/share/jenkins/ref/plugins/blueocean-*.hpi; do mv "$f" "$f.override"; done

USER jenkins
