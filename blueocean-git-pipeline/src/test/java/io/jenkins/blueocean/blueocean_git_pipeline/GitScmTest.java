package io.jenkins.blueocean.blueocean_git_pipeline;

import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.User;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
@RunWith(Parameterized.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class GitScmTest extends PipelineBaseTest {
    public static final String HTTPS_GITHUB_NO_JENKINSFILE = "https://github.com/vivek/test-no-jenkins-file.git";
    public static final String HTTPS_GITHUB_PUBLIC = "https://github.com/cloudbeers/multibranch-demo.git";
    public static final String HTTPS_GITHUB_PUBLIC_HASH = "996e1f714b08e971ec79e3bea686287e66441f043177999a13dbc546d8fe402a";
    // ^ is DigestUtils.sha256Hex(normalizedUrl)

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GitScmTest(String blueOrganisation) {
        System.out.println("setting org root to: " + blueOrganisation);
        TestOrganizationFactoryImpl.orgRoot = blueOrganisation;
    }

    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }

    private Map createCredentials(User user, Map credRequest) throws UnirestException {
        return new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/credentials/user/")
                .data(credRequest).build(Map.class);
    }

    private String needsGithubAccessToken(){
        String accessToken = System.getProperty("GITHUB_ACCESS_TOKEN");
        Assume.assumeTrue("GITHUB_ACCESS_TOKEN jvm property not set, ignoring test", accessToken != null);
        return accessToken;
    }

    @Test
    public void shouldCreateWithRemoteGitRepo() throws IOException, UnirestException {
        String accessToken = needsGithubAccessToken();
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = createCredentials(user, MapsHelper.of("credentials", new MapsHelper.Builder<String,Object>()
                .put("password", accessToken)
                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .put("scope", "USER")
                .put("domain","blueocean-git-domain")
                .put("description", "joe desc")
                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .put("username", "joe").build()
        ));

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);



        Map r = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", HTTPS_GITHUB_NO_JENKINSFILE,
                                                   "credentialId", credentialId)
                )).build(Map.class);

        assertEquals("demo", r.get("name"));

    }

    @Test
    public void shouldGetForbiddenForBadCredentialIdOnCreate1() throws IOException, UnirestException {
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = createCredentials(user, MapsHelper.of("credentials",
                new MapsHelper.Builder<String,Object>()
                        .put("privateKeySource", MapsHelper.of(
                                "privateKey", "abcabc1212",
                                "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                        .put("passphrase", "ssh2")
                        .put("scope", "USER")
                        .put("domain","blueocean-git-domain")
                        .put("description", "ssh2 desc")
                        .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                        .put("username", "ssh2").build()
        ));

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        post("/organizations/" + getOrgName() + "/pipelines/",
                MapsHelper.of("name", "demo",
                    "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                    "scmConfig", MapsHelper.of("uri", "git@github.com:vivek/capability-annotation.git",
                            "credentialId", credentialId)
                ), 400);

    }


    @Test
    public void shouldGetForbiddenForBadCredentialIdOnCreate2() throws IOException, UnirestException {
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = createCredentials(user, MapsHelper.of("credentials", new MapsHelper.Builder<String,Object>()
                .put("password", "abcd")
                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .put("scope", "USER")
                .put("domain","blueocean-git-domain")
                .put("description", "joe desc")
                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                .put("username", "joe").build()
        ));

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        post("/organizations/" + getOrgName() + "/pipelines/",
                MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 400);

    }

    @Test
    public void shouldGetBadRequestForBadGitUriOnCreate() throws Exception {

        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        post("/organizations/" + getOrgName() + "/pipelines/",
              MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", "/sdsd/sdsd/sdsd")
                ), 400);

    }


    @Test
    public void shouldFailForBadCredentialIdOnCreate() throws IOException, UnirestException {
        User user = login();
        Map resp = createCredentials(user, MapsHelper.of("credentials",
                new MapsHelper.Builder<String,Object>()
                        .put("privateKeySource", MapsHelper.of(
                                "privateKey", "abcabc1212",
                                "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                        .put("passphrase", "ssh2")
                        .put("scope", "USER")
                        .put("domain","blueocean-git-domain")
                        .put("description", "ssh2 desc")
                        .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                        .put("username", "ssh2").build()
                )
        );

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId))).build(Map.class);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(1, errors.size());
        assertEquals(errors.get(0).toString(), "scmConfig.credentialId", errors.get(0).get("field"));
        assertEquals(errors.get(0).toString(), "INVALID", errors.get(0).get("code"));
    }

    @Test
    public void shouldCreateGitMbp() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);

        assertEquals("demo", resp.get("name"));
    }

    @Test
    public void shouldFailOnValidation1(){
        Map<String,Object> resp = post("/organizations/" + getOrgName() + "/pipelines/",
                                        MapsHelper.of("scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl())
                ), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals("name", errors.get(0).get("field"));
        assertEquals("MISSING", errors.get(0).get("code"));
        assertEquals("$class", errors.get(1).get("field"));
        assertEquals("MISSING", errors.get(1).get("code"));
    }

    @Test
    public void shouldFailOnValidation2() throws Exception {

        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map<String,Object> resp = post("/organizations/" + getOrgName() + "/pipelines/",
                MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest"
                ), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals("scmConfig", errors.get(0).get("field"));
        assertEquals("MISSING", errors.get(0).get("code"));
        assertNull(getOrgRoot().getItem("demo"));
    }

    @Test
    public void shouldFailOnValidation3() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", Collections.emptyMap()))
                .build(Map.class);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(errors.get(0).get("field"), "scmConfig.uri");
        assertEquals(errors.get(0).get("code"), "MISSING");
        assertNull(getOrgRoot().getItem("demo"));

    }


    @Test
    public void shouldFailOnValidation4() throws IOException, UnirestException {
        login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of( "name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);


        assertEquals("demo", resp.get("name"));

        resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .crumb( crumb )
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl(),
                                "credentialId", "sdsdsd"))).build(Map.class);
        List<Map<String,String>> errors = (List<Map<String,String>>) resp.get("errors");

        boolean nameFound = false;
        boolean credentialIdFound = false;
        for(Map<String,String> error:errors){
            if(error.get("field").equals("name")){
                nameFound = true;
                assertEquals("ALREADY_EXISTS", error.get("code"));
            }else if(error.get("field").equals("scmConfig.credentialId")){
                credentialIdFound = true;
                assertEquals("NOT_FOUND", error.get("code"));
            }
        }
        assertTrue(nameFound);
        assertTrue(credentialIdFound);
    }

    @Test
    public void shouldFailOnValidation5() throws Exception {

        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map<String,Object> resp = post("/organizations/" + getOrgName() + "/pipelines/",
                MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl(), "credentialId", "sdsdsd")
                ), 400);
        List<Map<String,String>> errors = (List<Map<String,String>>) resp.get("errors");

        assertEquals("scmConfig.credentialId", errors.get(0).get("field"));
        assertEquals("NOT_FOUND", errors.get(0).get("code"));
        assertNull(getOrgRoot().getItem("demo"));
    }

    @Test
    public void shouldNotProvideIdForMissingCredentials() throws Exception {
        User user = login();
        String scmPath = "/organizations/" + getOrgName() + "/scm/git/";
        String repoPath = scmPath + "?repositoryUrl=" + HTTPS_GITHUB_PUBLIC;

        Map resp = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .get(repoPath)
            .build(Map.class);

        assertNull(resp.get("credentialId"));
    }

    @Test
    public void shouldBePoliteAboutBadUrl() throws Exception {
        User user = login();
        String scmPath = "/organizations/" + getOrgName() + "/scm/git/";
        // Let's say the user has only started typing a url
        String repoPath = scmPath + "?repositoryUrl=htt";

        Map resp = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .get(repoPath)
            .build(Map.class);

        assertNull(resp.get("credentialId"));
    }

    @Test
    public void shouldCreateCredentialsWithDefaultId() throws Exception {
        User user = login();

        String scmPath = "/organizations/" + getOrgName() + "/scm/git/";

        // First create a credential
        String scmValidatePath = scmPath + "validate";

        // We're relying on github letting you do a git-ls for repos with bad creds so long as they're public
        Map params = MapsHelper.of(
            "userName", "someguy",
            "password", "password",
            "repositoryUrl", HTTPS_GITHUB_PUBLIC
        );

        Map resp = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .data(params)
            .put(scmValidatePath)
            .build(Map.class);

        assertEquals("ok", resp.get("status"));

        // Now get the default credentialId

        String repoPath = scmPath + "?repositoryUrl=" + HTTPS_GITHUB_PUBLIC;

        Map resp2 = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .get(repoPath)
            .build(Map.class);

        assertEquals("git:" + HTTPS_GITHUB_PUBLIC_HASH, resp2.get("credentialId"));
    }

    /**
     * Check that we get an error when using an invalid URL
     * @throws Exception
     */
    @Test
    public void shouldNotCreateCredentialsForBadUrl1() throws Exception {
        User user = login();

        String scmPath = "/organizations/" + getOrgName() + "/scm/git/";

        // First create a credential
        String scmValidatePath = scmPath + "validate";

        // We're relying on github letting you do a git-ls for repos with bad creds so long as they're public
        Map params = MapsHelper.of(
            "userName", "someguy",
            "password", "password",
            "repositoryUrl", "htt"
        );

        Map resp = new RequestBuilder(baseUrl)
            .status(400)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .data(params)
            .put(scmValidatePath)
            .build(Map.class);

        assertTrue(resp.get("message").toString().toLowerCase().contains("invalid url"));
    }

    /**
     * Check that we get an error when using a valid but non-answering URL
     * @throws Exception
     */
    @Test
    public void shouldNotCreateCredentialsForBadUrl2() throws Exception {
        User user = login();

        String scmPath = "/organizations/" + getOrgName() + "/scm/git/";

        // First create a credential
        String scmValidatePath = scmPath + "validate";

        // We're relying on github letting you do a git-ls for repos with bad creds so long as they're public
        Map params = MapsHelper.of(
            "userName", "someguy",
            "password", "password",
            "repositoryUrl", "http://example.org/has/no/repos.git"
        );

        Map resp = new RequestBuilder(baseUrl)
            .status(428)
            .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
            .crumb( crumb )
            .data(params)
            .put(scmValidatePath)
            .build(Map.class);

        assertTrue(resp.get("message").toString().toLowerCase().contains("url unreachable"));
    }

    private String createMbp(User user) throws UnirestException {
        Map<String,Object> resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(MapsHelper.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", MapsHelper.of("uri", sampleRepo.fileUrl()))).build(Map.class);

        assertEquals("demo", resp.get("name"));
        Item item = getOrgRoot().getItem("demo");
        assertNotNull(item);
        assertTrue(item instanceof MultiBranchProject);
        return "demo";
    }

    private void setupScm() throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "stage 'build'\n "+"node {echo 'Building'}\n"+
                "stage 'test'\nnode { echo 'Testing'}\n"+
                "stage 'deploy'\nnode { echo 'Deploying'}\n"
        );
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature/ux-1");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
                "   stage ('Build'); " +
                "   echo ('Building'); " +
                "   stage ('Test'); " +
                "   echo ('Testing'); " +
                "   stage ('Deploy'); " +
                "   echo ('Deploying'); " +
                "}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");
    }



    private String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    private ModifiableTopLevelItemGroup getOrgRoot() {
        return OrganizationFactory.getItemGroup(getOrgName());
    }

    @TestExtension
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {

        public static String orgRoot;

        private OrganizationImpl instance;

        public TestOrganizationFactoryImpl() {
            System.out.println("TestOrganizationFactoryImpl org root is: " + orgRoot);
            setOrgRoot(orgRoot);
        }

        private void setOrgRoot(String root) {
            if (root != null) {
                try {
                    MockFolder itemGroup = Jenkins.get().createProject(MockFolder.class, root);
                    instance = new OrganizationImpl(root, itemGroup);
                } catch (IOException e) {
                    throw new RuntimeException("Test setup failed!", e);
                }

            }
            else {
                instance = new OrganizationImpl("jenkins", Jenkins.get());
            }
        }

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    System.out.println("" + name + " Instance returned " + instance);
                    return instance;
                }
            }
            System.out.println("" + name + " no instance found");
            return null;
        }

        @Override
        public Collection<BlueOrganization> list() {
            return Collections.singleton((BlueOrganization) instance);
        }

        @Override
        public OrganizationImpl of(ItemGroup group) {
            if (group == instance.getGroup()) {
                return instance;
            }
            return null;
        }
    }

}
