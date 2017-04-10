
export class DefaultSSEHandler {
    constructor(pipelineService, activityService, pagerService) {
        this.pipelineService = pipelineService;
        this.activityService = activityService;
        this.pagerService = pagerService;
    }

    handleEvents = (event) => {
        switch (event.jenkins_event) {
        case 'job_run_paused':
        case 'job_run_unpaused':
            this.updateJob(event);
            break;
        case 'job_crud_created':
            // Refetch pagers here. This will pull in the newly created pipeline into the bunker.
            this.pipelineService.refreshPagers();
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
            break;
        case 'job_run_queue_enter':
            this.queueEnter(event);
            break;
        case 'job_run_queue_left':
            this.queueLeft(event);
            break;
        case 'job_run_queue_blocked': {
            break;
        }
        case 'job_run_started': {
            this.updateJob(event, true);
            break;
        }
        case 'job_run_ended': {
            this.updateJob(event);
            break;
        }
        default :
        // Else ignore the event.
        }
    };

    branchPagerKeys(event) {
        if (!event.blueocean_job_branch_name) {
            return [this.activityService.pagerKey(event.jenkins_org, event.blueocean_job_pipeline_name)];
        }
        return [
            this.activityService.pagerKey(event.jenkins_org, event.blueocean_job_pipeline_name),
            this.activityService.pagerKey(event.jenkins_org, event.blueocean_job_pipeline_name, event.blueocean_job_branch_name),
        ];
    }

    updateJob(event, overrideQueuedState) {
        // const queueId = event.job_run_queueId;
        // const queueSelf = `${event.blueocean_job_rest_url}queue/${queueId}/`;
        const runSelf = `${event.blueocean_job_rest_url}runs/${event.jenkins_object_id}/`;

        for (const key of this.branchPagerKeys(event)) {
            const pager = this.pagerService.getPager({ key });
            this.activityService.fetchActivity(runSelf, { overrideQueuedState }).then(d => {
                if (pager && !pager.has(runSelf)) {
                    pager.insert(runSelf);
                }
                this.pipelineService.updateLatestRun(d);
            });
        }
    }
    queueCancel(event) {
        if (event.job_run_status === 'CANCELLED') {
            const queueId = event.job_run_queueId;
            const self = `${event.blueocean_job_rest_url}queue/${queueId}/`;
            this.activityService.removeItem(self);
        }
    }
    queueEnter(event) {
        // Ignore the event if there's no branch name. Usually indicates
        // that the event is wrt MBP indexing.
        if (event.job_ismultibranch && !event.blueocean_job_branch_name) {
            return;
        }

        const queueId = event.job_run_queueId;
        const self = `${event.blueocean_job_rest_url}queue/${queueId}/`;
        const id = this.activityService.getExpectedBuildNumber(event);

        const runSelf = `${event.blueocean_job_rest_url}runs/${id}/`;

        const newRun = {
            id,
            _links: {
                self: {
                    href: runSelf,
                },
                parent: {
                    href: event.blueocean_job_rest_url,
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
                    parent: {
                        href: event.blueocean_job_rest_url,
                    },
                },
            },
        };

        this.activityService.setItem(newRun);
        
        for (const key of this.branchPagerKeys(event)) {
            const pager = this.pagerService.getPager({ key });
            if (pager) {
                pager.insert(runSelf);
            }
        }
    }

    queueLeft(event) {
        if (event.job_run_status === 'CANCELLED') {
            const id = this.activityService.getExpectedBuildNumber(event);
            const runSelf = `${event.blueocean_job_rest_url}runs/${id}/`;
            this.activityService.removeItem(runSelf);
            for (const key of this.branchPagerKeys(event)) {
                const pager = this.pagerService.getPager({ key });
                if (pager) {
                    pager.remove(runSelf);
                }
            }
        }
    }
}
