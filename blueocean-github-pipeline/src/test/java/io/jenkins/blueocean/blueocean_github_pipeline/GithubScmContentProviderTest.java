package io.jenkins.blueocean.blueocean_github_pipeline;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Vivek Pandey
 */
public class GithubScmContentProviderTest extends GithubMockBase{

    @Test
    public void getContentForOrgFolder() throws UnirestException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder orgFolder = mockOrgFolder(credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    @Test
    public void getContentForMbp() throws UnirestException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder organizationFolder = mockOrgFolder(credentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    @Test
    public void saveContentToOrgFolder() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void saveContentToMbp() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(credentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void saveContentToMbpMissingBranch() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test2").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(credentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test2\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void testScmProperties() throws Exception {
        // ensure the cloud provider works with cloud org folder
        String credentialId = createGithubCredential();
        OrganizationFolder orgFolder = mockOrgFolder(credentialId);
        // unfortunately overriding the GitHub apiUrl for WireMock returns a "localhost" URL here, so we mock the call
        when(((GitHubSCMNavigator) orgFolder.getSCMNavigators().get(0)).getApiUri()).thenReturn(GitHubSCMSource.GITHUB_URL);
        ScmContentProvider provider = new GithubScmContentProvider();
        assertTrue(provider.support(orgFolder));
        assertEquals(provider.getScmId(), GithubScm.ID);
        assertEquals(provider.getApiUrl(orgFolder), GitHubSCMSource.GITHUB_URL);
        // ensure the cloud provider doesn't support enterprise org folder
        orgFolder = mockOrgFolder(createGithubEnterpriseCredential());
        assertFalse(provider.support(orgFolder));
    }

}
