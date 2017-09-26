package io.jenkins.blueocean.rest.impl.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.JsonConverter;
import jenkins.model.Jenkins;
import org.acegisecurity.adapters.PrincipalAcegiUserToken;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static io.jenkins.blueocean.auth.jwt.JwtToken.X_BLUEOCEAN_JWT;
import static org.junit.Assert.fail;

/**
 * @author Vivek Pandey
 */
public abstract class PipelineBaseTest extends PipelineBase {
    @BeforeClass
    public static void enableJWT() {
        System.setProperty("BLUEOCEAN_FEATURE_JWT_AUTHENTICATION", "true");
    }

    @AfterClass
    public static void resetJWT() {
        System.clearProperty("BLUEOCEAN_FEATURE_JWT_AUTHENTICATION");
    }

}
