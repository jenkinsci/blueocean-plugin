package io.jenkins.blueocean.rest.impl.pipeline;

import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.Item;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.MapsHelper;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vivek Pandey
 */
public class PipelineScmTest extends PipelineBaseTest {
    private User bob;
    private User alice;

    @Override
    public void setup() throws Exception {
        super.setup();
        this.bob = login();
        this.alice = User.get("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));
    }

    @Test
    public void unauthenticatedUserContentAccessShouldFail() throws IOException {
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "mbp");
        mp.setDisplayName("My MBP");
        new RequestBuilder(baseUrl)
                .status(401)
                .get("/organizations/jenkins/pipelines/mbp/scm/content").build(Map.class);

    }

    @Test
    public void AuthorizedUserContentAccessShouldSucceed() throws IOException, UnirestException {
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "mbp");
        mp.setDisplayName("My MBP");

        Map content = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, bob.getId(), bob.getId()))
                .get("/organizations/jenkins/pipelines/mbp/scm/content").build(Map.class);
        assertNotNull(content);
        assertEquals("Hello World!", ((Map)content.get("content")).get("data"));
    }

    @Test
    public void unauthenticatedUserContentUpdateShouldFail() throws IOException {
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "mbp");
        mp.setDisplayName("My MBP");
        new RequestBuilder(baseUrl)
                .status(401)
                .data(MapsHelper.of("content", MapsHelper.of("data", "Hello World Again!")))
                .put("/organizations/jenkins/pipelines/mbp/scm/content").build(Map.class)
        ;
    }

    @Test
    public void authorizedUserContentUpdateShouldSucceed() throws IOException, UnirestException {
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "mbp");
        mp.setDisplayName("My MBP");
        Map content = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, bob.getId(), bob.getId()))
                .data(MapsHelper.of("content", MapsHelper.of("data","Hello World Again!")))
                .put("/organizations/jenkins/pipelines/mbp/scm/content").build(Map.class);

        assertNotNull(content);
        assertEquals("Hello World Again!", ((Map)content.get("content")).get("data"));
    }




    public static class TestContent extends ScmContent{
        private final String data;

        public TestContent(String data) {
            this.data = data;
        }
        @Exported
        public String getData() {
            return data;
        }
    }

    @TestExtension
    public static class TestScmContentProvider extends ScmContentProvider{
        @NonNull
        @Override
        public String getScmId() {
            return "hello";
        }

        @CheckForNull
        @Override
        public String getApiUrl(@NonNull Item item) {
            return "https://hello.example.com";
        }

        @CheckForNull
        @Override
        public Object getContent(@NonNull StaplerRequest staplerRequest, @NonNull Item item) {
            return new ScmFile<TestContent>(){

                @Override
                public TestContent getContent() {
                    return new TestContent("Hello World!");
                }
            };
        }

        @CheckForNull
        @Override
        public Object saveContent(@NonNull StaplerRequest staplerRequest, @NonNull Item item) {
            try {
                JSONObject body = JSONObject.fromObject(IOUtils.toString(staplerRequest.getReader()));
                final String data = (String) ((Map)body.get("content")).get("data");
                return new ScmFile<TestContent>(){
                    @Override
                    public TestContent getContent() {
                        return new TestContent(data);
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean support(@NonNull Item item) {
            return item instanceof OrganizationFolder || item instanceof MultiBranchProject;
        }
    }
}
