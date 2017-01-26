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
    logger.debug('skewMillis', skewMillis);
    if (!run.startTime) {
        logger.error('not found any startTime, seems that a component should not have called this me');
        return {};
    }
    logger.warn('dates', Date.now(), run.startTime, new Date(moment(run.startTime)));
    const startTime = new Date(moment(run.startTime) - skewMillis).toJSON();
    const endTime = run.endTime ? new Date(moment(run.endTime) - skewMillis).toJSON() : null;
    logger.warn('startTime:', startTime, 'endTime:', endTime, 'run.startTime:', run.startTime);
    const start = Math.abs(moment(Date.now() - skewMillis).diff(moment(startTime)));
    const end = run.endTime ? moment().diff(moment(endTime)) : null;
    logger.debug('diff start:', start, 'diff end:', end);
    const durationMillis = run.isRunning && run.isRunning() ?
            start : run.durationInMillis;
    logger.debug('durationMillis:', durationMillis);
    logger.debug('running:', run.isRunning ? run.isRunning() : 'noComment');
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
