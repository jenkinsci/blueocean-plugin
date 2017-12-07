
export class DefaultSSEHandler {

    constructor(pipelineService, activityService, pagerService) {
        this.pipelineService = pipelineService;
        this.activityService = activityService;
        this.pagerService = pagerService;
        this.loggingEnabled = false;
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
        case 'job_run_queue_blocked':
            this.queueEnter(event);
            break;
        case 'job_run_queue_left':
            this.queueLeft(event);
            break;
        case 'job_run_started': {
            this.updateJob(event);
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
        const href = `${event.blueocean_job_rest_url}runs/${event.jenkins_object_id}/`;
        this._updateRun(event, href);
    }

    queueCancel(event) {
        if (event.job_run_status === 'CANCELLED') {
            const id = event.blueocean_queue_item_expected_build_number;
            const href = `${event.blueocean_job_rest_url}runs/${id}/`;
            this._removeRun(event, href);
        }
    }
    queueEnter(event) {
        // Ignore the event if there's no branch name. Usually indicates
        // that the event is wrt MBP indexing.
        if (event.job_ismultibranch && !event.blueocean_job_branch_name) {
            return;
        }
        // don't care about indexing events
        if (event.job_multibranch_indexing_status === 'INDEXING') {
            return;
        }
        // If we have a queued item but the branch isn't present, we need to refresh the pager
        // this happens, for example, when you create a new pipeline in a repo that did not have one
        const pipeline = this.pipelineService.getPipeline(`/blue/rest/organizations/${event.jenkins_org}/pipelines/${encodeURIComponent(event.blueocean_job_pipeline_name)}/`);
        if (pipeline && pipeline.branchNames.indexOf(event.blueocean_job_branch_name) === -1) {
            this.pipelineService.pipelinesPager(event.jenkins_org, event.blueocean_job_pipeline_name).refresh();
        }
        // Sometimes we can't match the queue item so we have to skip this event
        if (!event.blueocean_queue_item_expected_build_number) {
            return;
        }
        const id = event.blueocean_queue_item_expected_build_number;
        const href = `${event.blueocean_job_rest_url}runs/${id}/`;
        this._updateRun(event, href);
    }

    queueLeft(event) {
        // ignore the event if there's no build number
        // it's not related to a run, rather something like repo or branch indexing
        if (!event.blueocean_queue_item_expected_build_number) {
            return;
        }

        const id = event.blueocean_queue_item_expected_build_number;
        const href = `${event.blueocean_job_rest_url}runs/${id}/`;

        if (event.job_run_status === 'CANCELLED') {
            // Cancelled runs are removed from the stores. They are gone *poof*.
            this._removeRun(event, href);
        } else {
            // If not cancelled then the state may be leaving the queue to execute and should be updated with latest
            this._updateRun(event, href);
        }
    }

    /**
     * Removes the run from the activity service and any branch pagers
     * @param event triggering the removal
     * @param href of the run to remove
     * @private
     */
    _removeRun(event, href) {
        this.activityService.removeItem(href);
        for (const key of this.branchPagerKeys(event)) {
            const pager = this.pagerService.getPager({ key });
            if (pager) {
                pager.remove(href);
            }
        }
    }

    /**
     * Fetches the latest activity for this run, updates activity service and any branch pagers
     * @param event triggering the fetch
     * @param href of the run to add
     * @private
     */
    _updateRun(event, href) {
        const pipelineHref = this._computePipelineHref(event);
        const logMessage = `${event.jenkins_event} for pipeline ${pipelineHref} with run ${href}`;

        if (!this.pipelineService.hasItem(pipelineHref)) {
            this.loggingEnabled && console.log(`aborting fetch for ${logMessage}`);
            return;
        }

        this.loggingEnabled && console.log(`fetch ${logMessage}`);

        this.activityService.fetchActivity(href, { useCache: false, disableLoadingIndicator: true }).then((run) => {
            this.activityService.setItem(run);
            for (const key of this.branchPagerKeys(event)) {
                const pager = this.pagerService.getPager({ key });
                if (pager && !pager.has(href)) {
                    pager.insert(href);
                }
            }
            this.pipelineService.updateLatestRun(run);
        });
    }

    /**
     * Compute the REST URL / href for the job referenced in the supplied server side event.
     * @param event
     * @returns {string}
     * @private
     */
    _computePipelineHref(event) {
        let jobRestUrl = event.blueocean_job_rest_url;

        if (event.blueocean_job_branch_name) {
            // trim the last two path segments (e.g. 'branches/branch-name')
            jobRestUrl = jobRestUrl
                .split('/')
                .filter(p => p)
                .slice(0, -2)
                .join('/');
        }
        // ensure leading / trailing slashes
        if (jobRestUrl.slice(0, 1) !== '/') {
            jobRestUrl = `/${jobRestUrl}`;
        }
        if (jobRestUrl.slice(-1) !== '/') {
            jobRestUrl += '/';
        }
        return jobRestUrl;
    }
}
