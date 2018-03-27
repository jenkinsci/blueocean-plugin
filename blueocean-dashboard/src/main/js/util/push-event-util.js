/**
 * Enrich a 'job' channel event with some blueocean specific
 * data useful for the event processing.
 *
 * @param event The event object.
 * @param activePipelineName The pipeline name that's active on
 * the blueocean page, if any.
 * @returns The new enriched event instance.
 */
exports.enrichJobEvent = function(event, activePipelineName) {
    const eventCopy = Object.assign({}, event);

    // Is this event associated with the currently active pipeline job?
    eventCopy.blueocean_is_for_current_job = eventCopy.blueocean_job_pipeline_name === activePipelineName;

    return eventCopy;
};
