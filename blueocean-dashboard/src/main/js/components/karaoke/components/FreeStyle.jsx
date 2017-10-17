import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import LogConsole from './LogConsole';
import LogToolbar from './LogToolbar';
import { LogStore } from '../services/LogStore';

const logger = logging.logger('io.jenkins.blueocean.dashboard.following.FreeStyle');

@observer
export default class FreeStyle extends Component {
    /**
     * Mainly implemented because
     * - we need to fetch the log to display it
     */
    componentWillMount() {
        if (this.props.pipelineView) {
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
        if (
            (currentStart !== nextStart && nextStart !== undefined) ||
            (nextProps.run.isCompleted() && !this.props.run.isCompleted())
        ) {
            logger.debug('re-fetching the full log', this.props, nextProps);
            this.componentWillUnmount();
            this.fetchData(nextProps);
        }
    }
    componentWillUnmount() {
        logger.debug('stopping following mode, by removing the timeouts on the logStore.');
        this.props.liveUpdate.removeUpdater(this._updater);
    }
    fetchData(props) {
        const { pipelineView, location } = props;
        const start = location && location.query ? location.query.start : undefined;
        this.logStore = new LogStore(pipelineView.generalLogUrl, start);
        this.logStore.fetch();
        this.props.liveUpdate.addUpdater(this._updater = () => this.logStore.fetch());
    }
    render() {
        const { data: logArray, hasMore } = this.logStore.log;
        if (this.logStore.pending && logArray.length === 0) {
            logger.debug('abort due to logStore pending');
            return null;
        }
        const { t, router, location, pipelineView } = this.props;
        logger.debug('props', pipelineView.scrollToBottom, this.logStore.log.start, pipelineView.generalLogUrl);
        return (<div>
            <LogToolbar
                fileName={pipelineView.generalLogFileName}
                url={pipelineView.generalLogUrl}
                title={t('rundetail.pipeline.logs', { defaultValue: 'Logs' })}
            />
            <LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom: pipelineView.scrollToBottom,
                logArray,
                currentLogUrl: pipelineView.generalLogUrl,
                key: pipelineView.generalLogUrl,
            }}
            />
        </div>);
    }
}

FreeStyle.propTypes = {
    pipelineView: PropTypes.object,
    liveUpdate: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
    scrollToBottom: PropTypes.bool,
};
