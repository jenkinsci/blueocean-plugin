import { computed } from 'mobx';
import { AppConfig, capable, logging } from '@jenkins-cd/blueocean-core-js';

import { FREESTYLE_JOB, MULTIBRANCH_PIPELINE, PIPELINE_JOB } from '../../../Capabilities';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Augmenter');

function prefixIfNeeded(url) {
    return `${AppConfig.getJenkinsRootURL().replace(/\/$/, '')}${url}`;
}

/**
 * @export
 * @class Augmenter
 */
export class Augmenter {
    /**
     * The detail pager
     */
    @computed get href() {
        return prefixIfNeeded(this.run._links.self.href)
    };
    /**
     * Do we have a free style job?
     * @type {boolean}
     */
    @computed get isFreeStyle() {
        return capable(this.run, FREESTYLE_JOB);
    };
    /**
     * Do we have a pipeline job?
     * @type {boolean}
     */
    @computed get isPipeline() {
        return capable(this.run, PIPELINE_JOB);
    };
    /**
     * Do we have a multibranch pipeline job?
     * @type {boolean}
     */
    @computed get isMultiBranchPipeline(){
        return capable(this.pipeline, MULTIBRANCH_PIPELINE);
    };
    /**
     * What is the general log url?
     * @type {string}
     */
    @computed get generalLogUrl() {
        return prefixIfNeeded(this.run._links.log.href)
    };

    @computed get generalLogFileName(){
        if (this.isMultiBranchPipeline) {
            return `${this.branch}-${this.run.id}.txt`;
        } else {
            return `${this.run.id}.txt`;
        }
    };
    /**
     * Creates an instance of Augmenter
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     */
    constructor(pipeline, branch, run) {
        this.pipeline = pipeline;
        this.run = run;
        this.branch = branch;
    }

}
