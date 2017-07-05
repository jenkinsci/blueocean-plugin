package io.blueocean.ath;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import io.blueocean.ath.pages.classic.LoginPage;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;


/**
 * Adds additional functionality to Jenkins client API
 */
public class CustomJenkinsServer extends JenkinsServer {

    private final Logger logger = Logger.getLogger(getClass());

    protected final JenkinsHttpClient client;

    public CustomJenkinsServer(URI serverUri) {
        super(serverUri);
        // since JenkinsServer's "client" is private, we must create another one
        // use authenticated client so that user's credentials can be accessed
        client = new JenkinsHttpClient(serverUri, LoginPage.getUsername(), LoginPage.getPassword());
    }

    /**
     * Delete the credential stored in the specified user's domain.
     *
     * @param userName jenkins user name
     * @param domainName name of domain
     * @param credentialId credentialId
     * @throws IOException
     */
    public void deleteUserDomainCredential(String userName, String domainName, String credentialId) throws IOException {
        String path = "/user/" + userName + "/credentials/store/user/domain/" + domainName + "/credential/" + credentialId;

        try {
            client.get(path);
            logger.info("found credential at " + path);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() != 404) {
                throw e;
            }
            // credential doesn't exist; nothing to do
            return;
        }

        try {
            client.post(path + "/doDelete", false);
        } catch (HttpResponseException e) {
            logger.error("error deleting credential at " + path);
            logger.error("message = " + e.getMessage());
            throw e;
        }

        logger.info("deleted credential at " + path);
    }
}
