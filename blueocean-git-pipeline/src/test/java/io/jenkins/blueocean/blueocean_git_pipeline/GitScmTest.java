package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSampleRepoRule;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class GitScmTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }

    private Map createCredentials(User user, Map credRequest) throws UnirestException {
        return new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/jenkins/credentials/user/")
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

        Map resp = createCredentials(user, ImmutableMap.of("credentials", new ImmutableMap.Builder<String,Object>()
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
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "https://github.com/vivek/test-no-jenkins-file.git",
                                "credentialId", credentialId)
                )).build(Map.class);

        assertEquals("demo", r.get("name"));

    }

    @Test
    public void shouldGetForbiddenForBadCredentialIdOnCreate1() throws IOException, UnirestException {
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = createCredentials(user, ImmutableMap.of("credentials",
                new ImmutableMap.Builder<String,Object>()
                        .put("privateKeySource", ImmutableMap.of(
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

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 400);

    }


    @Test
    public void shouldGetForbiddenForBadCredentialIdOnCreate2() throws IOException, UnirestException {
        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        Map resp = createCredentials(user, ImmutableMap.of("credentials", new ImmutableMap.Builder<String,Object>()
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

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 400);

    }

    @Test
    public void shouldGetBadRequestForBadGitUriOnCreate() throws Exception {

        User user = login();
        this.jwtToken = getJwtToken(j.jenkins, user.getId(), user.getId());

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "/sdsd/sdsd/sdsd")
                ), 400);

    }


    @Test
    public void shouldFailForBadCredentialIdOnCreate() throws IOException, UnirestException {
        User user = login();
        Map resp = createCredentials(user, ImmutableMap.of("credentials",
                new ImmutableMap.Builder<String,Object>()
                        .put("privateKeySource", ImmutableMap.of(
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
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId))).build(Map.class);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(1, errors.size());
        assertEquals("scmConfig.credentialId", errors.get(0).get("field"));
        assertEquals("INVALID", errors.get(0).get("code"));
    }

    @Test
    public void shouldCreateGitMbp() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);

        assertEquals("demo", resp.get("name"));
    }

    @Test
    public void shouldFailOnValidation1(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of(
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
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

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest"
                ), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals("scmConfig", errors.get(0).get("field"));
        assertEquals("MISSING", errors.get(0).get("code"));
        assertNull(Jenkins.getInstance().getItem("demo"));
    }

    @Test
    public void shouldFailOnValidation3() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of()))
                .build(Map.class);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(errors.get(0).get("field"), "scmConfig.uri");
        assertEquals(errors.get(0).get("code"), "MISSING");
        assertNull(Jenkins.getInstance().getItem("demo"));

    }


    @Test
    public void shouldFailOnValidation4() throws IOException, UnirestException {
        login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ))
                .build(Map.class);


        assertEquals("demo", resp.get("name"));

        resp = new RequestBuilder(baseUrl)
                .status(400)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl(),
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

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl(), "credentialId", "sdsdsd")
                ), 400);
        List<Map<String,String>> errors = (List<Map<String,String>>) resp.get("errors");

        assertEquals("scmConfig.credentialId", errors.get(0).get("field"));
        assertEquals("NOT_FOUND", errors.get(0).get("code"));
        assertNull(Jenkins.getInstance().getItem("demo"));
    }

    private String createMbp(User user) throws UnirestException {
        Map<String,Object> resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,user.getId(), user.getId()))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl()))).build(Map.class);

        assertEquals("demo", resp.get("name"));
        Item item = Jenkins.getInstance().getItem("demo");
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
}
