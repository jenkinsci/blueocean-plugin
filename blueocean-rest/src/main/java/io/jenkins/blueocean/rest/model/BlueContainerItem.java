package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_PIPELINE_FOLDER;

/**
 * Common interface for items in the pipeline "namespace" that aggregate or contain other pipeline items, such as team
 * or organization folders, or the collection of pipelines that forms a BlueMultiBranchItem.
 * <p>
 * These may or may not be also runnable, so we want to keep that facet in BlueRunnableItem and not here.
 */
@Capability(BLUE_PIPELINE_FOLDER)
public interface BlueContainerItem /* extends BluePipelineItem */ {
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
    BlueIcon getIcon();

    /**
     * Returns pipeline folder names present in this folder.
     */
    @Exported(name = "pipelineFolderNames")
    Iterable<String> getPipelineFolderNames();
}
