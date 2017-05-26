package io.blueocean.ath.api.classic;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.BaseUrl;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Singleton
public class ClassicJobApi {

    private Logger logger = Logger.getLogger(ClassicJobApi.class);

    @Inject @BaseUrl
    String base;

    @Inject
    JenkinsServer jenkins;

    public void deletePipeline(String pipeline) throws IOException {
        try {
            jenkins.deleteJob(pipeline);
            logger.info("Deleted pipeline " + pipeline);
        } catch(HttpResponseException e) {
            if(e.getStatusCode() != 404) {
                throw e;
            }
        }
    }

    public void createFreeStyleJob(String jobName, String command) throws IOException {
        deletePipeline(jobName);
        URL url = Resources.getResource(this.getClass(), "freestyle.xml");
        jenkins.createJob(jobName, Resources.toString(url, Charsets.UTF_8).replace("{{command}}", command));
        logger.info("Created freestyle job "+ jobName);
    }

    public void createMultlBranchPipeline(String pipelineName, String repositoryPath) throws IOException {
        deletePipeline(pipelineName);
        URL url = Resources.getResource(this.getClass(), "multibranch.xml");
        jenkins.createJob(pipelineName, Resources.toString(url, Charsets.UTF_8).replace("{{repo}}", repositoryPath));
        logger.info("Created multibranch pipeline: "+ pipelineName);
        jenkins.getJob(pipelineName).build();
    }

    public <T> T until(Function<JenkinsServer, T> function, long timeoutInMS) {
        return new FluentWait<JenkinsServer>(jenkins)
            .pollingEvery(500, TimeUnit.MILLISECONDS)
            .withTimeout(timeoutInMS, TimeUnit.MILLISECONDS)
            .ignoring(NotFoundException.class)
            .until((JenkinsServer server) -> function.apply(server));
    }
}

