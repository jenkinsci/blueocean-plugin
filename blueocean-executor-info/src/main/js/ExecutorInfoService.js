import { observable, action } from 'mobx';
import { Fetch, UrlConfig, AppConfig, sseConnection } from '@jenkins-cd/blueocean-core-js';

export class ExecutorInfoService {
    @observable
    computers;
    
    constructor() {
        this.fetchExecutorInfo();
        sseConnection.subscribe('pipeline', event => {
            switch (event.jenkins_event) {
                case 'pipeline_block_start':
                case 'pipeline_block_end': {
                    this.fetchExecutorInfo();
                }
            }
        });
    }
    
    @action
    setComputers(computers) {
        this.computers = computers;
    }

    fetchExecutorInfo() {
        Fetch.fetchJSON(`${UrlConfig.getRestBaseURL()}/organizations/${AppConfig.getOrganizationName()}/computers/`)
        .then(response => {
            this.setComputers(response.computers);
        });
    }
}

// Export an instance to be shared so sseConnection.subscribe
// not called multiple times
export default new ExecutorInfoService();
