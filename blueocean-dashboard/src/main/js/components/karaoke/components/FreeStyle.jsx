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

    componentWillUnmount() {
        this.props.pager.clear();
    }

    fetchData(props) {
        const { pager, location, followAlong } = props;
        const pagerLogStart = pager.log ? pager.log.newStart : null;
        const start = location && location.query ? location.query.start : pagerLogStart;
        logger.warn('debugger', { pager, location, start });
        pager.fetchGeneralLog({ start, followAlong });
    }

    render() {
        const { pager, t, router, location, followAlong, scrollToBottom } = this.props;
        if (pager.logPending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        const { data: logArray, hasMore } = pager.log;
        logger.warn('props', scrollToBottom, this.props.pager.log.newStart, followAlong);
        return (<div>
            <LogToolbar
                fileName={pager.generalLogFileName}
                url={pager.generalLogUrl}
                title={t('rundetail.pipeline.logs', { defaultValue: 'Logs' })}
            />
            <LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                logArray,
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
    followAlong: PropTypes.bol,
    scrollToBottom: PropTypes.bol,
};
