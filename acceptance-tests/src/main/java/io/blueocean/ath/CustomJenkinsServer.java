package io.blueocean.ath;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;


/**
 * Adds additional functionality to Jenkins client API
 */
public class CustomJenkinsServer extends JenkinsServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JenkinsHttpClient client;

    public CustomJenkinsServer(URI serverUri, JenkinsUser admin) {
        super(serverUri, admin.username, admin.password);
        // since JenkinsServer's "client" is private, we must create another one
        // use authenticated client so that user's credentials can be accessed
        client = new JenkinsHttpClient(serverUri, admin.username, admin.password);
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
            client.post(path + "/doDelete", true);
            logger.info("deleted credential at {}", path);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                logger.debug("received 404 while trying to delete credential at {}", path);
            } else {
                logger.error("error deleting credential at {}", path);
                logger.error("message = {}", e.getMessage());
                throw e;
            }
        }

    }
}
