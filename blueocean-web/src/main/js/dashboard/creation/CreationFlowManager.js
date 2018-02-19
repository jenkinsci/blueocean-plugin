import { ToastService, AppConfig } from '@jenkins-cd/blueocean-core-js';
import FlowManager from './flow2/FlowManager';
import { pipelineService } from '@jenkins-cd/blueocean-core-js';

export default class CreationFlowManager extends FlowManager {
    changeState(stateId) {
        super.changeState(stateId);

        if (this.stateId === 'STEP_COMPLETE_MISSING_JENKINSFILE') {
            setTimeout(() => {
                pipelineService.refreshPagers();
                this._navigateToPipelineEditor();
                ToastService.newToast({ text: 'No Jenkinsfiles were found in this repository. Get started by creating one.' });
            }, this.redirectTimeout);
        }
    }

    _navigateToPipelineEditor() {
        const url = `/organizations/${AppConfig.getOrganizationName()}/pipeline-editor/${encodeURIComponent(this.pipelineName)}/`;
        this.completeFlow({ url });
    }
}
