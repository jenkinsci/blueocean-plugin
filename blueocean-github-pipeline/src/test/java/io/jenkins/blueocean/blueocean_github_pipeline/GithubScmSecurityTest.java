package io.jenkins.blueocean.blueocean_github_pipeline;

import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import io.jenkins.blueocean.commons.MapsHelper;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GithubScmSecurityTest extends GithubMockBase {

    private final static String ORGANIZATIONS_URL = "/organizations/jenkins/scm/github/organizations/";
    private final static String VALIDATE_URL = "/organizations/jenkins/scm/github/validate/";
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
    public void getOrganizationsForUserWithoutCredentials() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, READONLY_USER_NAME, READONLY_USER_PASSWORD);

        JSONObject res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .post(ORGANIZATIONS_URL + "?apiUrl=" + githubApiUrl)
            .status(400)
            .build(JSONObject.class);

        assertEquals("Credential id: " + GithubScm.ID + " not found for user " + READONLY_USER_NAME, res.getString("message"));
    }

    @Test
    public void getOrganizationsForUserWithItemCreatePermission() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, ITEM_CREATE_USER_NAME, ITEM_CREATE_USER_PASSWORD);
        createCredentialWithId(jwt, GithubScm.ID);

        JSONArray res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .post(ORGANIZATIONS_URL + "?apiUrl=" + githubApiUrl)
            .status(200)
            .build(JSONArray.class);

        assertEquals(6, res.size());
    }

    @Test
    public void getOrganizationsForUserWithoutItemCreatePermission() throws UnirestException {
        String jwt = getJwtToken(j.jenkins, READONLY_USER_NAME, READONLY_USER_PASSWORD);
        createCredentialWithId(jwt, GithubScm.ID);

        JSONObject res = request()
            .jwtToken(jwt)
            .crumb(crumb)
            .post(ORGANIZATIONS_URL + "?apiUrl=" + githubApiUrl)
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
            .data(MapsHelper.of("accessToken", accessToken))
            .post(VALIDATE_URL + "?apiUrl=" + githubApiUrl)
            .status(403)
            .build(JSONObject.class);

        assertEquals("You do not have Job/Create permission", res.getString("message"));
    }
}
