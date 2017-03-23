import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { KaraokeService } from '../index';
import LogConsole from './LogConsole';
import LogToolbar from './LogToolbar';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.FreeStyle');

@observer
export default class FreeStyle extends Component {
    /**
     * Mainly implemented because
     * - we need to fetch the log to display it
     */
    componentWillMount() {
        if (this.props.augmenter) {
            this.fetchData(this.props);
        }
    }
    /**
     * Mainly implemented because
     * - we need to re-fetch the log to display it when the log finished
     * - or when start is set to 0
     */
    componentWillReceiveProps(nextProps) {
        const nextStart = nextProps.location && nextProps.location.query ? nextProps.location.query.start : undefined;
        const currentStart = this.props.location && this.props.location.query ? this.props.location.query.start : undefined;
        logger.debug('newProps mate', nextProps, currentStart);
        if (!nextProps.augmenter.karaoke) {
            this.stopKaraoke();
        }
        if (
            (currentStart !== nextStart && nextStart !== undefined) ||
            (nextProps.run.isCompleted() && !this.props.run.isCompleted())
        ) {
            logger.debug('re-fetching since result changed and we want to display the full log');
            this.pager.fetchGeneralLog({ start: nextStart });
        }

    }

    componentWillUnmount() {
        this.stopKaraoke();
    }

    stopKaraoke() {
        logger.debug('stopping karaoke mode, by removing the timeouts on the pager.');
        this.pager.clear();
    }

    fetchData(props) {
        const { augmenter, location } = props;
        this.pager = KaraokeService.generalLogPager(augmenter, location);
    }

    render() {
        if (this.pager.pending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        const { t, router, location, scrollToBottom, augmenter } = this.props;
        const { data: logArray, hasMore } = this.pager.log;
        logger.debug('props', scrollToBottom, this.pager.log.newStart, augmenter.generalLogUrl);
        return (<div>
            <LogToolbar
                fileName={augmenter.generalLogFileName}
                url={augmenter.generalLogUrl}
                title={t('rundetail.pipeline.logs', { defaultValue: 'Logs' })}
            />
            <LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                logArray,
                key: augmenter.generalLogUrl,
            }}
            />
        </div>);
    }
}

FreeStyle.propTypes = {
    augmenter: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
    scrollToBottom: PropTypes.bool,
};
