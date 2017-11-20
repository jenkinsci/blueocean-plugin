package io.blueocean.ath.model;

import io.jenkins.blueocean.rest.model.BlueRun;

/**
 * Models how the UI combines BlueRunState and BlueRunResult enums.
 *
 * @see BlueRun
 * @author cliffmeyers
 */
public enum BlueJobStatus {

    SUCCESS,
    FAILURE,
    RUNNING,
    QUEUED,
    PAUSED,
    UNSTABLE,
    ABORTED,
    NOT_BUILT,
    SKIPPED,
    UNKNOWN

}
