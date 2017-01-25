import moment from 'moment';
import { logging } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.harmonizeTimes');
/**
 * we need to make sure that we calculate with the correct time offset
 *
 * @param run
 * @param skewMillis
 * @returns {{diff: {start: number, end: number}, durationMillis: diff, endTime: Date, startTime: Date}}
 */
export function harmonizeTimes(run, skewMillis) {
    const startTime = new Date(moment(run.startTime) - skewMillis).toJSON();
    const endTime = new Date(moment(run.endTime) - skewMillis).toJSON();
    logger.debug('startTime:', startTime, 'endTime:', endTime);
    const start = moment().diff(moment(startTime));
    const end = moment().diff(moment(endTime));
    logger.debug('diff start:', start, 'diff end:', end);
    const durationMillis = run.isRunning && run.isRunning() ?
            start : run.durationInMillis;
    logger.debug('durationMillis:', durationMillis);
    const newVar = {
        diff: {
            start,
            end,
        },
        durationMillis,
        endTime,
        startTime,
    };
    logger.debug('returning', newVar);
    return newVar;
}
