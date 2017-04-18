import moment from 'moment';
import logging from '../logging';

const logger = logging.logger('io.jenkins.blueocean.dashboard.harmonizeTimes');
/**
 * we need to make sure that we calculate with the correct time offset
 *
 * @param run
 * @param skewMillis
 * @returns {{ durationInMillis: diff, endTime: Date, startTime: Date}}
 */
export class TimeManager {
    currentTime() {
        return moment();
    }

    format(millis, hintFormat) {
        return moment.duration(millis).format(hintFormat);
    }

    /**
     *
     * @param props
     * @param skewMillis
     * @returns {
            durationInMillis,
            endTime,
            startTime,
        }
     */
    harmonizeTimes(props, skewMillis = 0) {
        logger.debug('skewMillis', skewMillis);
        if (!props.startTime) {
            logger.error('not found any startTime, seems that a component should not have called this me');
            return {};
        }
        // What time is it now on the client
        const clientTime = this.currentTime();
        // what is the start time of the server
        const serverStartTime = moment(props.startTime);
        // sync server start date to local time via the skewMillis
        if (skewMillis < 0) {
            serverStartTime.add({ milliseconds: Math.abs(skewMillis) });
        } else {
            serverStartTime.subtract({ milliseconds: skewMillis });
        }
        // export the harmonized start time
        const startTime = serverStartTime.toJSON();
        // how long has passed from the start to now in milliseconds
        const timeElapsed = clientTime.diff(serverStartTime, 'milliseconds');
        // assume we do not have an end date
        let endTime = null;
        let durationInMillis = 0;
        if (props.endTime) { // sync server end date to local time via the skewMillis
            const serverEndTime = moment(props.endTime);
            if (skewMillis < 0) {
                serverEndTime.add({ milliseconds: Math.abs(skewMillis) });
            } else {
                serverEndTime.subtract({ milliseconds: skewMillis });
            }
            endTime = serverEndTime.toJSON();
        }
        if (props.endTime || !props.isRunning) { // sync server end date to local time via the skewMillis
            durationInMillis = props.durationInMillis;
        } else {
            logger.debug('running, using timeElapsed for duration');
            durationInMillis = Math.abs(timeElapsed);
        }
        logger.debug('durationInMillis:', durationInMillis);
        const harmonized = {
            durationInMillis,
            endTime,
            startTime,
        };
        logger.debug('returning', harmonized);
        return harmonized;
    }
}
