import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
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
        debugger;
        if (nextProps.pager) {
            this.fetchData(nextProps);
        }
    }

    fetchData(props) {
        const { pager, location } = props;
        const fetchFullLog = location && location.query ? location.query.start === '0' : false;
        logger.warn('debugger', { pager, location, fetchFullLog });
        pager.fetchGeneralLog(fetchFullLog);
    }

    render() {
        logger.warn('props', this.props.pager.log);
        const { pager, t, router, location } = this.props;
        if (pager.logPending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        let logArray = [];
        const { data, hasMore } = pager.log;
        if (data && data.trim) {
            logArray = data.trim().split('\n');
        }
        return (<div>
            <LogToolbar
                fileName={pager.generalLogFileName}
                url={pager.generalLogUrl}
                title={t('rundetail.pipeline.logs', { defaultValue: 'Logs' })}
            />
            <LogConsole {...{
                logArray,
                t,
                router,
                location,
                hasMore,
                scrollToBottom: false,
                key: pager.generalLogUrl,
            }}
            />
        </div>);
    }
}

FreeStyle.propTypes = {
    pager: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    runId: PropTypes.string,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
};
