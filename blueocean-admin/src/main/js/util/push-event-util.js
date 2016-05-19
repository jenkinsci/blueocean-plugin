/**
 * Enrich a 'job' channel event with some blueocean specific
 * data useful for the event processing.
 *
 * @param event The event object.
 * @param activePipelineName The pipeline name that's active on
 * the blueocean page, if any.
 * @returns The new enriched event instance.
 */
exports.enrichJobEvent = function (event, activePipelineName) {
    const eventCopy = Object.assign({}, event);

    // For blueocean, we split apart the Job name and URL to get the
    // parts needed for looking up the correct pipeline, branch
    // and run etc.
    // TODO: what about nested folders ?
    const jobURLTokens = event.jenkins_object_url.split('/');
    const jobNameTokens = event.job_name.split('/');
    if (jobURLTokens[jobURLTokens.length - 1] === '') {
        // last token can be an empty string if the url has a trailing slash
        jobURLTokens.pop();
    }
    if (!isNaN(jobURLTokens[jobURLTokens.length - 1])) {
        // last/next-last token is a number (a build/run number)
        jobURLTokens.pop();
    }
    if (jobURLTokens.length > 3
        && jobURLTokens[jobURLTokens.length - 2] === 'branch') {
        // So it's a multibranch. The URL looks something like
        // "job/CloudBeers/job/PR-demo/branch/quicker/".
        // But we extract the job and branch name from event.job_name.
        eventCopy.blueocean_branch_name = jobNameTokens.pop();
        eventCopy.blueocean_is_multi_branch = true;
        eventCopy.blueocean_job_name = jobNameTokens.pop();
    } else {
        // It's not multibranch ... 1st token is the pipeline (job) name.
        // But we extract the job name from event.job_name.
        eventCopy.blueocean_job_name = jobNameTokens.pop();
        eventCopy.blueocean_is_multi_branch = false;
    }

    // Is this even associated with the currently active pipeline job?
    eventCopy.blueocean_is_for_current_job =
        (eventCopy.blueocean_job_name === activePipelineName);

    return eventCopy;
};
