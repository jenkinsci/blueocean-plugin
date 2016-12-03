import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';

class PipelineStepListStore {
    getStepListing(handler) {
        if (this.stepData) {
            handler(this.stepData);
            return;
        }
        Fetch.fetchJSON(`${UrlConfig.getBlueOceanAppURL()}/rest/pipeline-metadata/pipelineStepMetadata?depth=20`).then(data => {
            this.stepData = this.filterStepListing(data);
            handler(this.stepData);
        });
    }
    filterStepListing(steps) {
        return steps;
    }
}

const pipelineStepListStore = new PipelineStepListStore();

export default pipelineStepListStore;
