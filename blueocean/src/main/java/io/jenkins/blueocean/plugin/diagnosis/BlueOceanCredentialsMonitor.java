package io.jenkins.blueocean.plugin.diagnosis;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AdministrativeMonitor;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.HttpResponses;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Extension
@Symbol("bo-credentials")
@Restricted( NoExternalUse.class)
public class BlueOceanCredentialsMonitor extends AdministrativeMonitor
{

    private static final Logger LOGGER = LoggerFactory.getLogger( BlueOceanCredentialsProvider.class);

    /**
     * contains the list of jobs affected by disabling BlueOceanCredentialsProvider
     * key/value: displayName/url
     */
    private final Map<String,String> projectsNamesUrlsWithBoFolderCred = new ConcurrentHashMap<>();

    private static final long SCANNING_INTERVAL =
        SystemProperties.getLong(BlueOceanCredentialsMonitor.class.getName() + ".scanning.interval.minutes", 30L);

    @Override
    public String getDisplayName() {
        return "Blue Ocean credentials provider";
    }

    public boolean isBlueOceanCredentialsProvidedEnabled() {
        return ExtensionList.lookupSingleton(BlueOceanCredentialsProvider.class).isEnabled( null );
    }


    @Override
    public boolean isActivated() {
        return !projectsNamesUrlsWithBoFolderCred.isEmpty() || isBlueOceanCredentialsProvidedEnabled();
    }

    @Override
    public boolean isSecurity() {
        return true;
    }

    public Map<String,String> getProjectsNamesUrlsWithBoFolderCred() {
        return projectsNamesUrlsWithBoFolderCred;
    }

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void scanBlueOceanCredentials() {
        if (SCANNING_INTERVAL < 1) {
            LOGGER.info("BlueOceanCredentialsMonitor scanning is deactivated");
            return;
        }
        Timer.get().scheduleAtFixedRate(this::scanInstance, 0, SCANNING_INTERVAL, TimeUnit.MINUTES);
    }

    public HttpResponse doScan() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        this.scanInstance();
        return HttpResponses.redirectToDot();
    }


    private void scanInstance() {
        LOGGER.debug("scan multibranch projects");
        List<WorkflowMultiBranchProject> projects = Jenkins.get().getItems( WorkflowMultiBranchProject.class );
        Map<String,String> projectsNamesUrls = new HashMap<>();
        for (WorkflowMultiBranchProject project : projects) {
            for (SCMSource scmSource : project.getSCMSources()) {
                try {
                    Method getCredentialsIdMethod =
                        scmSource.getClass().getDeclaredMethod( "getCredentialsId", null );
                    String credentialsId = (String) getCredentialsIdMethod.invoke( scmSource, null );
                    BlueOceanCredentialsProvider.FolderPropertyImpl property =
                        project.getProperties().get( BlueOceanCredentialsProvider.FolderPropertyImpl.class );
                    if (property != null) {
                        // property id is same as credentials and this is a credentials stored at USER scope
                        if(StringUtils.equals(property.getId(), credentialsId)
                            && findCredentials(property.getUser(), credentialsId) != null) {
                            projectsNamesUrls.put(project.getDisplayName(), project.getUrl());
                        }
                    }
                } catch (NoSuchMethodException e) {
                    LOGGER.debug( "SCMSource: {} do not have method getCredentialsId", scmSource.getClass().getName());
                } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                    LOGGER.debug( "SCMSource: '" + scmSource.getClass().getName() + "' cannot retrieve credentialId via getCredentialsId", e);
                }
            }
        }

        projectsNamesUrlsWithBoFolderCred.clear();
        projectsNamesUrlsWithBoFolderCred.putAll(projectsNamesUrls);

        LOGGER.debug("end scan multibranch projects, projects found {}", projectsNamesUrls.size());
    }

    private Credentials findCredentials( String userId, String credentialsId ) {
        User proxyUser = User.get(userId, false, Collections.emptyMap());
        try (ACLContext ignored = ACL.as( proxyUser.impersonate())) {
            StandardCredentials standardCredentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(StandardCredentials.class, Jenkins.get(),
                                                      Jenkins.getAuthentication(),
                                                      (List<DomainRequirement>) null),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsId),
                                          CredentialsMatchers.withScope(CredentialsScope.USER)));
            if (standardCredentials != null) {
                LOGGER.debug( "find standardCredentials for user {} with id {} and description {}", userId,
                              credentialsId, standardCredentials.getDescription() );
            } else {
                LOGGER.debug( "find standardCredentials for user {} with id {}", userId, credentialsId);
            }
            return standardCredentials;
        }
    }



}
