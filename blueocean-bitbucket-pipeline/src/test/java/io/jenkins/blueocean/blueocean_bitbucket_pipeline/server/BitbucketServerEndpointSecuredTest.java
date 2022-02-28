package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.mashape.unirest.http.HttpResponse;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpointSecuredTest
    extends PipelineBaseTest {
    private static final String URL = "/organizations/jenkins/scm/bitbucket-server/servers/";
    User readUser, writeUser;

    @Before
    public void setupSecurity() throws Exception {
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(true);
        readUser = realm.createAccount("read_user", "pacific_ale");
        writeUser = realm.createAccount("write_user", "pale_ale");
        j.jenkins.setSecurityRealm(realm);
        GlobalMatrixAuthorizationStrategy as = new GlobalMatrixAuthorizationStrategy();
        j.jenkins.setAuthorizationStrategy(as);

        as.add( Jenkins.READ, (String)Jenkins.ANONYMOUS.getPrincipal());

        {
            as.add(Jenkins.READ, readUser.getId());
        }
        {
            as.add( Item.BUILD, writeUser.getId());
            as.add(Item.CREATE, writeUser.getId());
            as.add(Item.CONFIGURE, writeUser.getId());
        }
        this.crumb = getCrumb(j.jenkins );
    }

    @Test
    public void createAndListFailAnonymous() throws Exception {
        HttpResponse<String> response = request()
            .crumb( crumb )
            .data(MapsHelper.of(
                "name", "My Server",
                "apiUrl", "https://foo.com/git/"
            ))
            .post(URL)
            .build().asString();
        assertEquals(403, response.getStatus());
    }

    @Test
    public void createPermissionFail() throws Exception {

        HttpResponse<String> response = request()
            .crumb( crumb )
            .jwtToken( getJwtToken( j.jenkins, readUser.getId(), "pacific_ale") )
            .data(MapsHelper.of(
                "name", "My Server",
                "apiUrl", "https://foo.com/git/"
            ))
            .post(URL)
            .build().asString();
        assertEquals(403, response.getStatus());
        assertEquals("Forbidden", response.getStatusText());
        assertTrue(response.getBody().contains( "User does not have permission to create repository"));
    }

    @Test
    public void createPermissionSuccessButMissingValue() throws Exception {
        // we only test we passed authorisation check and get bad request response
        HttpResponse<String> response = request()
            .crumb( crumb )
            .jwtToken( getJwtToken( j.jenkins, writeUser.getId(), "pale_ale") )
            //.auth("read_user", "readonlymate")
            .data(MapsHelper.of(
                "name", "",
                "apiUrl", "https://foo.com/git/"
            ))
            .post(URL)
            .build().asString();
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getStatusText());
        assertTrue(response.getBody().contains(ScmServerEndpoint.NAME + " is required"));
    }
}
