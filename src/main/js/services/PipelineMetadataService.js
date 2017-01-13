import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';

class PipelineMetadataService {
    cache: object = {};

    /**
     * Caches locally
     */
    _fetch(method, handler) {
        if (this.cache[method]) {
            handler(this.cache[method]);
            return;
        }
        Fetch.fetchJSON(`${UrlConfig.getBlueOceanAppURL()}/rest/pipeline-metadata/${method}?depth=20`).then(data => {
            this.cache[method] = data;
            handler(this.cache[method]);
        });
    }
    
    getStepListing(handler) {
        this._fetch('pipelineStepMetadata', data => {
            const filtered = this.filterStepListing(data);
            handler(filtered);
        });
    }

    filterStepListing(steps) {
        return steps;
    }

    getAgentListing(handler) {
        this._fetch('agentMetadata', data => handler(data));
    }
}

const pipelineMetadataService = new PipelineMetadataService();

export default pipelineMetadataService;
