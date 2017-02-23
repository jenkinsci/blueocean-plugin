import { action, computed, observable } from 'mobx';
import { capable } from '@jenkins-cd/blueocean-core-js';
import { prefixIfNeeded } from '../urls/prefixIfNeeded';

import { FREESTYLE_JOB, MULTIBRANCH_PIPELINE, PIPELINE_JOB } from '../../../Capabilities';


/**
 * @export
 * @class Augmenter
 */
export class Augmenter {
    /**
     * The detail pager
     */
    @computed get href() {
        return prefixIfNeeded(this.run._links.self.href);
    }
    /**
     * What is the general log url?
     * @type {string}
     */
    @computed get generalLogUrl() {
        return prefixIfNeeded(this.run._links.log.href);
    }
    /**
     * nodes ref or null
     * @type {string | null}
     */
    @computed get nodesUrl() {
        if (this.run._links.nodes) {
            return prefixIfNeeded(this.run._links.nodes.href);
        }
        return null;
    }
    /**
     * steps ref or null
     * @type {string | null}
     */
    @computed get stepsUrl() {
        if (this.run._links.steps) {
            return prefixIfNeeded(this.run._links.steps.href);
        }
        return null;
    }
    /**
     * Do we have a free style job?
     * @type {boolean}
     */
    @computed get isFreeStyle() {
        return capable(this.run, FREESTYLE_JOB);
    }
    /**
     * Do we have a pipeline job?
     * @type {boolean}
     */
    @computed get isPipeline() {
        return capable(this.run, PIPELINE_JOB);
    }
    /**
     * Do we have a multibranch pipeline job?
     * @type {boolean}
     */
    @computed get isMultiBranchPipeline() {
        return capable(this.pipeline, MULTIBRANCH_PIPELINE);
    }

    @computed get generalLogFileName() {
        if (this.isMultiBranchPipeline) {
            return `${this.branch}-${this.run.id}.txt`;
        }
        return `${this.run.id}.txt`;
    }

    @observable karaoke = false;
    @observable run;
    @observable branch;
    @observable pipeline;

    /**
     * Creates an instance of Augmenter
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     * @param {boolean} followAlong should we follow along
     */
    constructor(pipeline, branch, run, followAlong) {
        this.setPipeline(pipeline);
        this.setBranch(branch);
        this.setKaraoke(followAlong);
        this.setRun(run);
    }

    @action
    setKaraoke(followAlong) {
        this.karaoke = followAlong;
    }

    @action
    setRun(run) {
        this.run = run;
    }

    @action
    setPipeline(pipeline) {
        this.pipeline = pipeline;
    }

    @action
    setBranch(branch) {
        this.branch = branch;
    }
}
