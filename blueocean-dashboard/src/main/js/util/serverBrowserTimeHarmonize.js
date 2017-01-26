import moment from 'moment';
import { logging } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.harmonizeTimes');
/**
 * we need to make sure that we calculate with the correct time offset
 *
 * @param run
 * @param skewMillis
 * @returns {{ durationMillis: diff, endTime: Date, startTime: Date}}
 */
export class TimeManager {
    currentTime() {
        return moment();
    }

    harmonizeTimes(run, skewMillis) {
        logger.warn('skewMillis', skewMillis);
        if (!run.startTime) {
            logger.error('not found any startTime, seems that a component should not have called this me');
            return {};
        }
    // What time is it now on the client
        const clientTime = this.currentTime();
    // what is the start time of the server
        const serverStartTime = moment(run.startTime);
    // sync server start date to local time via the skewMillis
        if (skewMillis < 0) {
            serverStartTime.subtract({ milliseconds: skewMillis * -1 });
        } else {
            serverStartTime.add({ milliseconds: skewMillis });
        }
    // export the harmonized start time
        const startTime = serverStartTime.toJSON();
    // how long has passed from the start to now in milliseconds
        const timeElapsed = clientTime.diff(serverStartTime, 'milliseconds');
    // assume we do not have an end date
        let endTime = null;
        let durationMillis = 0;
        if (run.endTime) { // sync server end date to local time via the skewMillis
            const serverEndTime = moment(run.endTime);
            if (skewMillis < 0) {
                serverEndTime.subtract({ milliseconds: skewMillis * -1 });
            } else {
                serverEndTime.add({ milliseconds: skewMillis });
            }
            endTime = serverEndTime.toJSON();
        }
        if (run.endTime || !run.isRunning) { // sync server end date to local time via the skewMillis
            durationMillis = run.durationInMillis;
        } else {
            logger.debug('running, using timeElapsed for duration');
            durationMillis = timeElapsed;
        }
        logger.debug('durationMillis:', durationMillis);
        const harmonized = {
            durationMillis,
            endTime,
            startTime,
        };
        logger.debug('returning', harmonized);
        return harmonized;
    }
}
