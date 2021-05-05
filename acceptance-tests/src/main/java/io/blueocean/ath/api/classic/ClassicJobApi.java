package io.blueocean.ath.api.classic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.Crumb;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.JenkinsUser;
import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.model.Folder;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Singleton
public class ClassicJobApi {

    private static final Logger LOGGER = LoggerFactory.getLogger( ClassicJobApi.class);

    @Inject @BaseUrl
    String base;

    @Inject
    public JenkinsServer jenkins;

    @Inject
    JenkinsUser admin;

    public void deletePipeline(String pipeline) throws IOException {
        deletePipeline(null, pipeline);
    }

    public void deletePipeline(FolderJob folder, String pipeline) throws IOException {
        try {
            jenkins.deleteJob(folder, pipeline, true);
            LOGGER.info("Deleted pipeline " + pipeline);
        } catch(HttpResponseException e) {
            if(e.getStatusCode() != 404) {
                throw e;
            }
        }
    }

    public void deleteFolder(Folder folder) throws IOException {
        if (folder.getFolders().size() == 1) {
            jenkins.deleteJob(folder.getPath(), true);
        } else {
            throw new UnsupportedOperationException("deleting a nested folder is not supported");
        }
    }

    public void deleteFolder(String folder) throws IOException {
        try {
            jenkins.deleteJob(folder, true);
            LOGGER.info( "Deleted folder " + folder);
        } catch(HttpResponseException e) {
            if(e.getStatusCode() != 404) {
                throw e;
            }
        }
    }



    public void createFreeStyleJob(FolderJob folder, String jobName, String command) throws IOException {
        deletePipeline(folder, jobName);
        URL url = Resources.getResource(this.getClass(), "freestyle.xml");
        jenkins.createJob(folder, jobName, Resources.toString(url, Charsets.UTF_8).replace("{{command}}", command), true);
        LOGGER.info( "Created freestyle job "+ jobName);
    }

    public void createFreeStyleJob(String jobName, String command) throws IOException {
        deletePipeline(jobName);
        URL url = Resources.getResource(this.getClass(), "freestyle.xml");
        jenkins.createJob(null, jobName, Resources.toString(url, Charsets.UTF_8).replace("{{command}}", command), true);
        LOGGER.info( "Created freestyle job "+ jobName);
    }

    public void createPipeline(FolderJob folder, String jobName, String script) throws IOException {
        deletePipeline(folder, jobName);
        URL url = Resources.getResource(this.getClass(), "pipeline.xml");
        jenkins.createJob(folder, jobName, Resources.toString(url, Charsets.UTF_8).replace("{{script}}", script), true);
        LOGGER.info( "Created pipeline job "+ jobName);
    }
    public void createMultiBranchPipeline(FolderJob folder, String pipelineName, String repositoryPath) throws IOException {
        deletePipeline(folder, pipelineName);
        URL url = Resources.getResource(this.getClass(), "multibranch.xml");
        jenkins.createJob(folder, pipelineName, Resources.toString(url, Charsets.UTF_8).replace("{{repo}}", repositoryPath), true);
        LOGGER.info( "Created multibranch pipeline: "+ pipelineName);
        JobWithDetails job = jenkins.getJob(folder, pipelineName);
        job.build(true);
    }

    public FolderJob createJobFolder(String name, String jobUrl) throws IOException, UnirestException {
        if(StringUtils.isBlank(jobUrl) || jobUrl.equals("/")){
            jobUrl = base+"/";
        }
        URL url = Resources.getResource(this.getClass(), "folder.xml");

        Crumb crumb = getCrumb();
        Unirest.post(jobUrl+"createItem?name="+name).
                        header("Content-Type", "text/xml").
                        basicAuth(admin.username, admin.password).
                        header(crumb.getCrumbRequestField(), crumb.getCrumb()).
                        body(Resources.toByteArray(url)).asString();
        LOGGER.info( "Created folder: "+ name);
        return new FolderJob(name, jobUrl+"job/"+name+"/");
    }

    public FolderJob createSubFolder(Folder parentFolder, String name) throws IOException, UnirestException {
        return createJobFolder(name, parentFolder.getClassJobPath());
    }

    private void createFolderImpl(Job folder, String folderName) throws IOException {
        String path = base + "/";
        if (folder != null) {
            path = folder.getUrl().replace("+", "%20");
        }
        path += "createItem";
        ImmutableMap<String, Object> params = ImmutableMap.of("mode", "com.cloudbees.hudson.plugins.folder.Folder",
            "name",   folderName, "from", "", "Submit", "OK");
        try {
            Crumb crumb = getCrumb();
            Unirest.post(path).basicAuth(admin.username, admin.password).
                header(crumb.getCrumbRequestField(), crumb.getCrumb()).
                fields(params).asString();
        } catch (UnirestException e) {
            throw new IOException(e);
        }



    }

    private static ObjectMapper getDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private static final ObjectMapper OM = getDefaultMapper();

    private static final UnirestObjectMapper UOM = new UnirestObjectMapper();

    private static class UnirestObjectMapper implements com.mashape.unirest.http.ObjectMapper {
        @Override
        public <T> T readValue( String s, Class<T> aClass ) {
            try {
                return OM.readValue(s, aClass);
            } catch (IOException e) {
                LOGGER.info("Failed to parse JSON: {}. {}", s, e.getMessage());
                throw new RuntimeException(e);
            }
        }

        @Override
        public String writeValue( Object o ) {
            try {
                return OM.writeValueAsString(o);
            } catch ( JsonProcessingException e ) {
                LOGGER.info("Failed to write Object: {}. {}",o,e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    static {
        Unirest.setObjectMapper(UOM);
    }

    protected Crumb getCrumb() throws IOException {
        String path = base + "/crumbIssuer/api/json";
        try
        {
            Crumb crumb = Unirest.get(path).
                basicAuth(admin.username, admin.password).
                asObject(Crumb.class).
                getBody();
            return crumb;
        } catch (UnirestException e) {
            throw new IOException(e.getMessage(), e);
        }

    }

    public FolderJob getFolder(Folder folder, boolean createMissing) throws IOException {
        if(folder == null || folder.getFolders().size() == 0) {
            return null;
        }

        Job job = jenkins.getJob(folder.get(0));
        if(job == null && createMissing) {
            createFolderImpl(null, folder.get(0));
            job = jenkins.getJob(folder.get(0));
        }
        FolderJob ret = jenkins.getFolderJob(job).get();

        for (int i = 1; i < folder.getFolders().size(); i++) {
            job = jenkins.getJob(ret, folder.get(i));
            if(job == null && createMissing) {
                createFolderImpl(ret, folder.get(i));
                job = jenkins.getJob(ret, folder.get(i));
            }
            ret = jenkins.getFolderJob(job).get();
        }

        return ret;
    }

    public void createMultiBranchPipeline(FolderJob folder, String pipelineName, GitRepositoryRule repository) throws IOException {
        createMultiBranchPipeline(folder, pipelineName, repository.gitDirectory.getAbsolutePath());
    }

    public FolderJob createFolders(Folder folder, boolean deleteRoot) throws IOException, UnirestException {
        if(deleteRoot) {
            deleteFolder(folder.get(0));
        }
        FolderJob lastFolder = createJobFolder(folder.get(0), "/");

        for (int i = 1; i < folder.getFolders().size(); i++) {
            String subFolderName = folder.get(i);
            lastFolder = createJobFolder(subFolderName, lastFolder.getUrl());
        };
        return lastFolder;
    }

    public <T> T until(Function<JenkinsServer, T> function, long timeoutInMS) {
        return new FluentWait<>(jenkins)
            .pollingEvery(500, TimeUnit.MILLISECONDS)
            .withTimeout(timeoutInMS, TimeUnit.MILLISECONDS)
            .ignoring(NotFoundException.class)
            .until((JenkinsServer server) -> function.apply(server));
    }

    public void buildBranch(Folder folder, String pipeline, String branch) throws IOException {
        jenkins.getJob(getFolder(folder.append(pipeline), false), branch).build(true);
    }

    public void build(Folder folder, String pipeline) throws IOException {
        jenkins.getJob(getFolder(folder, false), pipeline).build(true);
    }
    public void abortAllBuilds(Folder folder, String pipeline) throws IOException {
        JobWithDetails job = jenkins.getJob(getFolder(folder, false), pipeline);

        for(Build build: job.getBuilds()){
            if(build.details().getResult() == null) {
                build.details().Stop();
                LOGGER.info( "Stopped build " + folder.getPath( pipeline) + " - #" + build.getNumber());
            }
        }

        Optional<FolderJob> folderJobOptional = jenkins.getFolderJob(job);

        if(folderJobOptional.isPresent()) {
            for (String s : folderJobOptional.get().getJobs().keySet()) {
                abortAllBuilds(folder.append(pipeline), s);
            }
        }
    }


    public com.google.common.base.Function<WebDriver, Boolean> untilJobResultFunction(AbstractPipeline pipeline, BuildResult desiredResult) {
        return driver -> {
            try {
                JobWithDetails job = ClassicJobApi.this.jenkins.getJob(ClassicJobApi.this.getFolder(pipeline.getFolder(), false), pipeline.getName());
                BuildResult result = job.getLastBuild().details().getResult();
                return result == desiredResult;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        };
    }
}

