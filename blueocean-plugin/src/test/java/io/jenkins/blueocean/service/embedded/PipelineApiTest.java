package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.tasks.Shell;
import io.jenkins.blueocean.commons.JsonConverter;
import org.junit.Assert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class PipelineApiTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before() {
        RestAssured.baseURI = j.jenkins.getRootUrl()+"bo/rest";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void getPipelineTest() throws IOException {
        j.createFreeStyleProject("pipeline1");

        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline1")
            .then().log().all()
            .statusCode(200)
            .body("organization", Matchers.equalTo("jenkins"))
            .body("name", Matchers.equalTo("pipeline1"));
    }

    @Test
    public void deletePipelineTest() throws IOException {
        Project p = j.createFreeStyleProject("pipeline1");

        RestAssured.given().log().all().delete("/organizations/jenkins/pipelines/pipeline1/")
            .then().log().all()
            .statusCode(200);

        Assert.assertNull(j.jenkins.getItem(p.getName()));
    }


    @Test
    public void getPipelinesTest() throws IOException {
        Project p1 = j.createFreeStyleProject("pipeline1");
        Project p2 = j.createFreeStyleProject("pipeline2");
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/")
            .then().log().all()
            .statusCode(200)
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(p1.getName()))
            .body("[0].displayName", Matchers.equalTo(p1.getDisplayName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(p2.getName()))
            .body("[1].displayName", Matchers.equalTo(p2.getDisplayName()));
    }


    @Test
    public void findPipelinesTest() throws IOException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline2");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline3");

        RestAssured.given().log().all().get("/search?q=type:pipeline;organization:jenkins")
            .then().log().all()
            .statusCode(200)
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].name", Matchers.equalTo(p1.getName()))
            .body("[1].organization", Matchers.equalTo("jenkins"))
            .body("[1].name", Matchers.equalTo(p2.getName()));
    }

    @Test
    public void getPipelineRunTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline4");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline4/runs/"+b.getId())
            .then().log().all()
            .statusCode(200)
            .body("id", Matchers.equalTo(b.getId()))
            .body("pipeline", Matchers.equalTo(p.getName()))
            .body("pipeline", Matchers.equalTo(p.getName()))
            .body("organization", Matchers.equalTo("jenkins"))
            .body("startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }

    @Test
    public void getPipelineRunLatestTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline5");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);

        RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins;pipeline:pipeline5;latestOnly:true")
            .then().log().all()
            .statusCode(200)
            .body("[0].id", Matchers.equalTo(b.getId()))
            .body("[0].pipeline", Matchers.equalTo(p.getName()))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }

    @Test
    public void getPipelineRunsTest() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("pipeline6");
        p.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        System.out.println(new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis())));
        RestAssured.given().log().all().get("/organizations/jenkins/pipelines/pipeline6/runs")
            .then().log().all()
            .statusCode(200)
            .body("[0].id", Matchers.equalTo(b.getId()))
            .body("[0].pipeline", Matchers.equalTo(p.getName()))
            .body("[0].organization", Matchers.equalTo("jenkins"))
            .body("[0].startTime", Matchers.equalTo(
                new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
    }

    @Test
    public void findPipelineRunsForAPipelineTest() throws Exception {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline1");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline2");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> builds = new Stack<FreeStyleBuild>();
        FreeStyleBuild b11 = p1.scheduleBuild2(0).get();
        FreeStyleBuild b12 = p1.scheduleBuild2(0).get();
        builds.push(b11);
        builds.push(b12);

        j.assertBuildStatusSuccess(b11);
        j.assertBuildStatusSuccess(b12);

        ValidatableResponse response = RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins;pipeline:pipeline1")
            .then().log().all()
            .statusCode(200);

        for (int i = 0; i < builds.size(); i++) {
            FreeStyleBuild b = builds.pop();
            response.body(String.format("[%s].id",i), Matchers.equalTo(b.getId()))
                .body(String.format("[%s].pipeline",i), Matchers.equalTo(b.getParent().getName()))
                .body(String.format("[%s].organization",i), Matchers.equalTo("jenkins"))
                .body(String.format("[%s].startTime",i), Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
        }
    }

    @Test
    public void findPipelineRunsForAllPipelineTest() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject p1 = j.createFreeStyleProject("pipeline11");
        FreeStyleProject p2 = j.createFreeStyleProject("pipeline22");
        p1.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        p2.getBuildersList().add(new Shell("echo hello!\nsleep 1"));
        Stack<FreeStyleBuild> p1builds = new Stack<FreeStyleBuild>();
        p1builds.push(p1.scheduleBuild2(0).get());
        p1builds.push(p1.scheduleBuild2(0).get());

        Stack<FreeStyleBuild> p2builds = new Stack<FreeStyleBuild>();
        p2builds.push(p2.scheduleBuild2(0).get());
        p2builds.push(p2.scheduleBuild2(0).get());

        Map<String, Stack<FreeStyleBuild>> buildMap = ImmutableMap.of(p1.getName(), p1builds, p2.getName(), p2builds);

        Response r = RestAssured.given().log().all().get("/search?q=type:run;organization:jenkins");
        ValidatableResponse response = r
            .then().log().all()
            .statusCode(200);

        for (int i = 0; i < 4; i++) {
            String pipeline = r.path(String.format("[%s].pipeline",i));
            FreeStyleBuild b = buildMap.get(pipeline).pop();
            response.body(String.format("[%s].id",i), Matchers.equalTo(b.getId()))
                .body(String.format("[%s].pipeline",i), Matchers.equalTo(pipeline))
                .body(String.format("[%s].organization",i), Matchers.equalTo("jenkins"))
                .body(String.format("[%s].startTime",i), Matchers.equalTo(
                    new SimpleDateFormat(JsonConverter.DATE_FORMAT_STRING).format(new Date(b.getStartTimeInMillis()))));
        }
    }

}
