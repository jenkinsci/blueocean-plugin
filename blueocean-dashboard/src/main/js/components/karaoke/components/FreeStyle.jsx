import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { KaraokeService } from '../index';
import LogConsole from '../../LogConsole';
import LogToolbar from '../../LogToolbar';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.FreeStyle');

@observer
export default class FreeStyle extends Component {
    componentWillMount() {
        if (this.props.augmenter) {
            this.fetchData(this.props);
        }
    }

    componentWillReceiveProps(nextProps) {

        if(!nextProps.followAlong && this.props.followAlong) {
            this.stopKaraoke();
        }
        if (nextProps.run.isCompleted() && !nextProps.augmenter.run.isCompleted()) {
            logger.debug('re-fetching since result changed and we want to display the full log');
            this.pager.fetchGeneralLog({});
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
        const { augmenter, followAlong } = props;
        this.pager = KaraokeService.karaokePager(augmenter, followAlong);
    }

    render() {
        const { t, router, location, followAlong, scrollToBottom } = this.props;
        if (this.pager.pending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        const { data: logArray, hasMore } = this.pager.log;
        logger.warn('props', scrollToBottom, this.pager.log.newStart, followAlong);
        return (<div>
            <LogToolbar
                fileName={this.pager.generalLogFileName}
                url={this.pager.generalLogUrl}
                title={t('rundetail.pipeline.logs', { defaultValue: 'Logs' })}
            />
            <LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                logArray,
                key: this.pager.generalLogUrl,
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
    followAlong: PropTypes.bol,
    scrollToBottom: PropTypes.bol,
};
