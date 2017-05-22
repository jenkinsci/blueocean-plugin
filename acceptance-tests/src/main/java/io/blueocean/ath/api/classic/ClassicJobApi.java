package io.blueocean.ath.api.classic;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.JenkinsServer;
import io.blueocean.ath.BaseUrl;
import org.apache.http.client.HttpResponseException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;

@Singleton
public class ClassicJobApi {

    @Inject @BaseUrl
    String base;

    @Inject
    JenkinsServer jenkins;

    public void deleteJob(String jobName) throws IOException {
        try {
            jenkins.deleteJob(jobName);
        } catch(HttpResponseException e) {
            if(e.getStatusCode() != 404) {
                throw e;
            }
        }
    }

    public void createFreeStyleJob(String jobName, String command) throws IOException {
        deleteJob(jobName);
        URL url = Resources.getResource(this.getClass(), "freestyle.xml");
        jenkins.createJob(jobName, Resources.toString(url, Charsets.UTF_8).replace("{{command}}", command));
    }


}

