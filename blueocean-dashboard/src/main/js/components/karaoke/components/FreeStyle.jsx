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
        if (this.props.pager) {
            this.fetchData(this.props);
        }
    }

    componentWillReceiveProps(nextProps) {
        debugger
        if (nextProps.pager) {
            this.fetchData(nextProps);
        }
    }

    fetchData(props) {
        const { pager} = props;
        logger.warn('debugger');
        pager.fetchGeneralLog();
    }

    render() {
        logger.warn('props', this.props.pager.log);
        const { pager } = this.props;
        if(pager.logPending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        debugger
        const logProps = {
            hasMore: pager.log.hasMore,
            logArray: pager.log.data,
            ...this.props,
            scrollToBottom: false,
            key: pager.generalLogUrl,
            t: (value) => value,
        };
        return (<div>
            <LogToolbar
                fileName={pager.generalLogFileName}
                url={pager.generalLogUrl}
                title={`title`}
            />
            <LogConsole {...logProps} />
        </div>);
    }
}

FreeStyle.propTypes = {
    pager: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    runId: PropTypes.string,
};
