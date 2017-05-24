package io.blueocean.ath.api.classic;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.BaseUrl;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;

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


}

