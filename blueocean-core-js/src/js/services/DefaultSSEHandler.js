
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

    updateJob(event) {
        // const queueId = event.job_run_queueId;
        // const queueSelf = `${event.blueocean_job_rest_url}queue/${queueId}/`;
        const runSelf = `${event.blueocean_job_rest_url}runs/${event.jenkins_object_id}/`;
        this.updateRun(runSelf);
    }

    queueCancel(event) {
        if (event.job_run_status === 'CANCELLED') {
            const id = event.blueocean_queue_item_expected_build_number;
            const self = `${event.blueocean_job_rest_url}runs/${id}/`;
            this.activityService.removeItem(self);
        }
    }
    queueEnter(event) {
        // Ignore the event if there's no branch name. Usually indicates
        // that the event is wrt MBP indexing.
        if (event.job_ismultibranch && !event.blueocean_job_branch_name) {
            return;
        }

        const id = event.blueocean_queue_item_expected_build_number;
        const runSelf = `${event.blueocean_job_rest_url}runs/${id}/`;

        this.updateRun(runSelf);

        for (const key of this.branchPagerKeys(event)) {
            const pager = this.pagerService.getPager({ key });
            if (pager) {
                pager.insert(runSelf);
            }
        }
    }

    queueLeft(event) {
        if (event.job_run_status === 'CANCELLED') {
            const id = event.blueocean_queue_item_expected_build_number;
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

    updateRun(runUrl) {
        this.activityService.fetchActivity(runUrl, { useCache: false });
        for (const key of this.branchPagerKeys(event)) {
            const pager = this.pagerService.getPager({ key });
            this.activityService.fetchActivity(runUrl, { useCache: false }).then(d => {
                if (pager && !pager.has(runUrl)) {
                    pager.insert(runUrl);
                }
                this.pipelineService.updateLatestRun(d);
            });
        }
    }
}
