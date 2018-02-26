package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_PIPELINE_FOLDER;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_ABSTRACT_FOLDER;

/**
 * Folder  has pipelines, could also hold another BluePipelineFolders.
 *
 * BluePipelineFolder subclasses BluePipeline in order to handle recursive pipelines path:
 *
 * /pipelines/f1/pipelines/f2/pipelines/p1
 *
 *
 * @author Vivek Pandey
 *
 * @see BluePipelineContainer
 */
@Capability({BLUE_PIPELINE_FOLDER, JENKINS_ABSTRACT_FOLDER})
public abstract class BluePipelineFolder extends BluePipeline {

    private static final String NUMBER_OF_PIPELINES = "numberOfPipelines";
    private static final String NUMBER_OF_FOLDERS = "numberOfFolders";
    private static final String PIPELINE_NAMES = "pipelineNames";
    private static final String PIPELINE_FOLDER_NAMES = "pipelineFolderNames" ;

    /**
     * @return Gives pipeline container
     */
    public abstract BluePipelineContainer getPipelines();

    /**
     *
     * Gets nested BluePipeline inside the BluePipelineFolder
     *
     * For example for: /pipelines/folder1/pipelines/folder2/pipelines/p1, call sequence  will be:
     *
     * <ul>
     *     <li>getPipelines().get("folder1")</li>
     *     <li>getPipelines().get(folder2)</li>
     *     <li>getDynamics(p1)</li>
     * </ul>
     *
     * @param name name of pipeline
     *
     * @return a {@link BluePipeline}
     */
    public BluePipeline getDynamic(String name){
        return getPipelines().get(name);
    }


    /**
     * @return Number of folders in this folder
     */
    @Exported(name = NUMBER_OF_FOLDERS)
    public abstract Integer getNumberOfFolders();


    /**
     * @return Number of pipelines in this folder. Pipeline is any buildable type.
     */
    @Exported(name = NUMBER_OF_PIPELINES)
    public abstract Integer getNumberOfPipelines();


    @Override
    @Exported(skipNull = true)
    public Integer getWeatherScore() {
        return null;
    }

    @Override
    @Exported(skipNull = true)
    public BlueRun getLatestRun() {
        return null;
    }

    @Override
    @Exported(skipNull = true)
    public Long getEstimatedDurationInMillis() {
        return null;
    }

    @Override
    public BlueRunContainer getRuns() {
        return null;
    }

    @Override
    public BlueQueueContainer getQueue() {
        return null;
    }

    @Exported(skipNull = true)
    public abstract BlueIcon getIcon();

    /**
     * Returns pipeline folder names present in this folder.
     */
    @Exported(name=PIPELINE_FOLDER_NAMES)
    public abstract Iterable<String> getPipelineFolderNames();
}
