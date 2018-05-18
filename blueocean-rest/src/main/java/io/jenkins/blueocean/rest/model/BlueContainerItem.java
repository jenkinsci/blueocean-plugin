package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

public interface BlueContainerItem {
    /**
     * @return Gives pipeline container
     */
    BluePipelineContainer getPipelines();

    /**
     * Gets nested BluePipeline inside the BluePipelineFolder
     * <p>
     * For example for: /pipelines/folder1/pipelines/folder2/pipelines/p1, call sequence  will be:
     *
     * <ul>
     * <li>getPipelines().get("folder1")</li>
     * <li>getPipelines().get(folder2)</li>
     * <li>getDynamics(p1)</li>
     * </ul>
     *
     * @param name name of pipeline
     * @return a {@link BluePipeline}
     */
    BluePipeline getDynamic(String name);

    /**
     * @return Number of folders in this folder
     */
    @Exported(name = "numberOfFolders")
    Integer getNumberOfFolders();

    /**
     * @return Number of pipelines in this folder. Pipeline is any buildable type.
     */
    @Exported(name = "numberOfPipelines")
    Integer getNumberOfPipelines();

    @Exported(skipNull = true)
    BlueIcon getIcon(); // TODO: Can't seem to find any @Exported properties on any subclasses of BlueIcon, why even is this?

    /**
     * Returns pipeline folder names present in this folder.
     */
    @Exported(name = "pipelineFolderNames")
    Iterable<String> getPipelineFolderNames();
}
