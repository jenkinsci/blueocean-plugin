import { AppPaths, RestPaths } from '../utils/paths';
import utils from '../utils';
export class DefaultSSEHandler {
    constructor(pipelineService, activityService, pagerService) {
        this.pipelineService = pipelineService;
        this.activityService = activityService;
        this.pagerService = pagerService
    }

    handleEvents = (event) => {
        console.log('_sseEventHandler', event);
        switch (event.jenkins_event) {
        case 'job_crud_created':
            // Refetch pagers here. This will pull in the newly created pipeline into the bunker.
            this.pipelineService.refresh(this.bunker);
            break;
        case 'job_crud_deleted':
            // Remove directly from bunker. No need to refresh bunkers as it will just show one less item.
            this.pipelineService.removeItem(event.blueocean_job_rest_url);
            break;
        case 'job_crud_renamed':
            // TODO: Implement this.
            // Seems to be that SSE fires an updated event for the old job,
            // then a rename for the new one. This is somewhat confusing for us.
            break;
        case 'job_run_queue_buildable':
        case 'job_run_queue_enter':
            console.log('queue_event');
            this.queueEvent(event);
            break;
        case 'job_run_queue_left':
           // this.props.processJobLeftQueueEvent(eventCopy);
            break;
        case 'job_run_queue_blocked': {
            break;
        }
        case 'job_run_started': {
           // this.props.updateRunState(eventCopy, this.context.config, true);
           // this.props.updateBranchState(eventCopy, this.context.config);
            break;
        }
        case 'job_run_ended': {
           // this.props.updateRunState(eventCopy, this.context.config);
           // this.props.updateBranchState(eventCopy, this.context.config);
            break;
        }
        default :
        // Else ignore the event.
        }
    }

    queueEvent(event) {
        const queueId = event.job_run_queueId;
        const self = `${AppPaths.getJenkinsRootURL}${event.blueocean_job_rest_url}queue/${queueId}/`;
        console.log('self', self);
        const newRun = {
            id: 9999,
            _links: {
                self: {
                    href: self,
                },
            },
            job_run_queueId: queueId,
            pipeline: event.blueocean_job_branch_name,
            result: 'UNKNOWN',
            state: 'QUEUED',
            _item: {
                _links: {
                    self: {
                        href: self,
                    },
                },
            },
        };

        this.activityService.setItem(newRun);
        const key = this.activityService.pagerKey(event.jenkins_org ,event.blueocean_job_pipeline_name);
        const pager = this.pagerService.getPager({ key });
        console.log('pager', pager);
        if (pager) {
            pager.insert(self);
        }
    }
}