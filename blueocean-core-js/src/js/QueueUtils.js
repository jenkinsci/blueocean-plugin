/**
 * Created by cmeyers on 9/23/16.
 */

/**
 * This function maps a queue item into a run instancce.
 *
 * We do this because the api returns us queued items as well
 * as runs and its easier to deal with them if they are modeled
 * as the same thing. If the raw data is needed if can be fetched
 * from _queueItem.
 */
function mapQueueImplToPseudoRun(run) {
    if (run._class === 'io.jenkins.blueocean.service.embedded.rest.QueueItemImpl') {
        return {
            _links: run._links,
            id: String(run.expectedBuildNumber),
            state: 'QUEUED',
            pipeline: run.pipeline,
            type: 'QueuedItem',
            result: 'UNKNOWN',
            job_run_queueId: run.id,
            enQueueTime: run.queuedTime,
            organization: run.organization,
            changeSet: [],
            _queueItem: run,
        };
    }
    return run;
}

export default {
    mapQueueImplToPseudoRun,
};
