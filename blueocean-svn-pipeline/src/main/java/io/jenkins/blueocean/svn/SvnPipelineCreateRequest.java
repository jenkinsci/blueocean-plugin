package io.jenkins.blueocean.svn;

import com.google.common.collect.Lists;
import io.jenkins.blueocean.commons.ErrorMessage.Error;
import io.jenkins.blueocean.rest.impl.pipeline.AbstractMultiBranchCreateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMSource;
import jenkins.scm.impl.subversion.SubversionSCMSource;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;

public class SvnPipelineCreateRequest extends AbstractMultiBranchCreateRequest {

    @DataBoundConstructor
    public SvnPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    protected SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig) {
        SubversionSCMSource source = new SubversionSCMSource(null, StringUtils.defaultString(scmConfig.getUri()));
        source.setCredentialsId(StringUtils.defaultString(scmConfig.getCredentialId()));
        source.setIncludes("trunk,branches/*,tags/*,sandbox/*");
        return source;
    }

    @Override
    protected List<Error> validate(String name, BlueScmConfig scmConfig) {
        List<Error> errors = Lists.newArrayList();

        // TODO: validate that the uri is valid and we can connect to the repository
//        SVNRepository repository = null;
//
//        try {
//            // the way it works with SVNKit is that
//            // 1) svnkit calls AuthenticationManager asking for a credential.
//            //    this is when we can see the 'realm', which identifies the user domain.
//            // 2) DefaultSVNAuthenticationManager returns the username and password we set below
//            // 3) if the authentication is successful, svnkit calls back acknowledgeAuthentication
//            //    (so we store the password info here)
//            repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(scmConfig.getUri()));
//            repository.setTunnelProvider( createDefaultSVNOptions() );
//            AuthenticationManagerImpl authManager = new UserProvidedCredential.AuthenticationManagerImpl(null);
//            authManager.setAuthenticationForced(true);
//            repository.setAuthenticationManager(authManager);
//            repository.testConnection();
//            authManager.checkIfProtocolCompleted();
//        } catch (SVNCancelException e) {
//            e.printStackTrace();
//        } catch (SVNException e) {
//            e.printStackTrace();
//        } finally {
//            if (repository != null)
//                repository.closeSession();
//        }

        return errors;
    }
}
