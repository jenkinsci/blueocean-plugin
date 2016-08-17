import React, { Component, PropTypes } from 'react';
import LogConsole from './LogConsole';

import LogToolbar from './LogToolbar';

import {
    calculateRunLogURLObject,
} from '../util/UrlUtils';
const { bool, string, object, func } = PropTypes;


export default class LogConsoleView extends Component {

    componentWillMount() {
        const { fetchLog, mergedConfig } = this.props;
        console.log('sanity?');
        // console.log('fetch the log directly')
        const logGeneral = calculateRunLogURLObject(mergedConfig);
        // fetchAll indicates whether we want all logs
        const fetchAll = mergedConfig.fetchAll;
        fetchLog({ ...logGeneral, fetchAll });
    }

    componentWillReceiveProps(nextProps) {
        const { fetchLog, mergedConfig, logs, followAlong } = nextProps;
        const fetchAll = mergedConfig.fetchAll;
        // if we only interested in logs (in case of e.g. freestyle)
        if (logs !== this.props.logs || fetchAll) {
            const logGeneral = calculateRunLogURLObject(mergedConfig);
            // console.log('logGenralReceive', logGeneral)
            const log = logs ? logs[logGeneral.url] : null;
            if (log && log !== null) {
                // we may have a streaming log
                const newStart = log.newStart;
                if (Number(newStart) > 0) {
                    // in case we doing karaoke we want to see more logs
                    if (followAlong) {
                        // kill current  timeout if any
                        clearTimeout(this.timeout);
                        // we need to get mpre input from the log stream
                        this.timeout = setTimeout(() => fetchLog({ ...logGeneral, newStart }), 1000);
                    }
                }
            } else if (fetchAll) {
                // kill current  timeout if any
                clearTimeout(this.timeout);
                // we need to get mpre input from the log stream
                this.timeout = setTimeout(() => fetchLog({ ...logGeneral, fetchAll }), 1000);
            }
        }
    }


    componentWillUnmount() {
        clearTimeout(this.timeout);
    }

    render() {
        const { logs, mergedConfig, followAlong, title = 'Logs', scrollToBottom } = this.props;
        const logGeneral = calculateRunLogURLObject(mergedConfig);
        const log = logs ? logs[logGeneral.url] : null;
        const logProps = {
            scrollToBottom,
            key: logGeneral.url,
        };
        if (log) {
            // in follow along the Full Log button should not be shown, since you see everything already
            if (followAlong) {
                logProps.hasMore = false;
            } else {
                logProps.hasMore = log.hasMore;
            }
            logProps.logArray = log.logArray;
            return (<div>
                <LogToolbar
                  fileName={logGeneral.fileName}
                  url={logGeneral.url}
                  title={title}
                />
                <LogConsole {...logProps} />
            </div>);
        }
        return null;
    }
}


LogConsoleView.propTypes = {
    result: object,
    mergedConfig: object,
    fetchLog: func,
    followAlong: bool,
    title: string,
    scrollToBottom: bool,
    logs: object,
    steps: object,
    nodes: object,
    nodeReducer: object,
};
