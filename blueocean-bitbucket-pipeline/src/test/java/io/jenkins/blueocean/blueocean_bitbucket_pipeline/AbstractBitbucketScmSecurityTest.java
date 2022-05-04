package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BbServerWireMock;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScm;
import io.jenkins.blueocean.commons.MapsHelper;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractBitbucketScmSecurityTest extends BbServerWireMock {
    private static final String ORGANIZATIONS_URL = "/organizations/jenkins/scm/" + BitbucketServerScm.ID + "/organizations/";
    private static final String VALIDATE_URL = "/organizations/jenkins/scm/" + BitbucketServerScm.ID + "/validate/";
    public static final String READONLY_USER_NAME = "readOnly";
    public static final String READONLY_USER_PASSWORD = "pacific_ale";
    public static final String ITEM_CREATE_USER_NAME = "itemCreateUser";
    public static final String ITEM_CREATE_USER_PASSWORD = "pale_ale";

    @Before
    public void setupSecurity() throws Exception {
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(true, false, null);
        User readOnly = realm.createAccount(READONLY_USER_NAME, READONLY_USER_PASSWORD);
        User itemCreateUser = realm.createAccount(ITEM_CREATE_USER_NAME, ITEM_CREATE_USER_PASSWORD);
        j.jenkins.setSecurityRealm(realm);
        GlobalMatrixAuthorizationStrategy as = new GlobalMatrixAuthorizationStrategy();
        j.jenkins.setAuthorizationStrategy(as);
        as.add(Jenkins.READ, (String) Jenkins.ANONYMOUS2.getPrincipal());
        as.add(Jenkins.READ, readOnly.getId());
        as.add(Item.CREATE, itemCreateUser.getId());
        this.crumb = getCrumb(j.jenkins);
    }

    @Test
    public void getOrganizationsWithoutCrumbToken() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, ITEM_CREATE_USER_NAME, ITEM_CREATE_USER_PASSWORD);

        String res = request()
            .jwtToken(jwt)
            .post(ORGANIZATIONS_URL)
            .status(403)
            .build(String.class);

        assertTrue(res.contains("No valid crumb was included in the request"));
    }

    @Test
    public void getOrganizationsWithGetRequest() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, ITEM_CREATE_USER_NAME, ITEM_CREATE_USER_PASSWORD);

        JSONObject res = request()
            .jwtToken(jwt)
            .get(ORGANIZATIONS_URL)
            .status(405)
            .build(JSONObject.class);

        assertEquals("Request method GET is not allowed", res.getString("message"));
    }

    @Test
    public void getOrganizationsForUserWithItemCreatePermission() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, ITEM_CREATE_USER_NAME, ITEM_CREATE_USER_PASSWORD);
        String credentialId = "totally-unique-credentialID";
        createCredentialWithId(jwt, credentialId);

        JSONArray res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .post(ORGANIZATIONS_URL + "?apiUrl=" + apiUrl + "&credentialId=" + credentialId)
            .status(200)
            .build(JSONArray.class);

        assertEquals(3, res.size());
    }

    @Test
    public void getOrganizationsForUserWithoutItemCreatePermission() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, READONLY_USER_NAME, READONLY_USER_PASSWORD);
        String credentialId = "readonly-permissions";
        createCredentialWithId(jwt, credentialId);

        JSONObject res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .header(BitbucketServerScm.X_CREDENTIAL_ID, credentialId)
            .post(ORGANIZATIONS_URL + "?apiUrl=" + apiUrl)
            .status(403)
            .build(JSONObject.class);

        assertEquals("You do not have Job/Create permission", res.getString("message"));
    }

    @Test
    public void validateAndCreateForUserWithReadonlyPermissions() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, READONLY_USER_NAME, READONLY_USER_PASSWORD);

        JSONObject res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .data(MapsHelper.of("apiUrl", apiUrl,
                "userName", ITEM_CREATE_USER_NAME,
                "password", ITEM_CREATE_USER_PASSWORD))
            .post(VALIDATE_URL + "?apiUrl=" + apiUrl)
            .status(403)
            .build(JSONObject.class);

        String errorMessage = res.getString("message");
        assertEquals("You do not have Job/Create permission", errorMessage);
    }
}
