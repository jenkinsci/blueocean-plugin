import React, { Component, PropTypes } from 'react';
import { ResultItem } from '@jenkins-cd/design-language';
import { calculateLogUrl } from '../util/UrlUtils';

import LogConsole from './LogConsole';

const { object, func, string, bool } = PropTypes;

export default class Node extends Component {
    componentWillMount() {
        const { nodesBaseUrl, fetchLog } = this.props;
        const { config = {} } = this.context;
        const node = this.expandAnchor(this.props);
        if (node && node.isFocused) {
            const mergedConfig = { ...config, node, nodesBaseUrl };
            fetchLog(mergedConfig);
        }
    }

    componentWillReceiveProps(nextProps) {
        const { logs, nodesBaseUrl, fetchLog, followAlong } = nextProps;
        const { config = {} } = this.context;
        const node = this.expandAnchor(nextProps);
        const mergedConfig = { ...config, node, nodesBaseUrl };
        if (logs && logs !== this.props.logs) {
            const key = calculateLogUrl(mergedConfig);
            const log = logs ? logs[key] : null;
            if (log && log !== null) {
                // we may have a streaming log
                const number = Number(log.newStart);
                // in case we doing karaoke we want to see more logs
                if (number > 0 && followAlong) {
                    mergedConfig.newStart = log.newStart;
                    // kill current  timeout if any
                    this.clearThisTimeout();
                    this.timeout = setTimeout(() => fetchLog({ ...mergedConfig }), 1000);
                }
            }
        }
    }

    componentWillUnmount() {
        this.clearThisTimeout();
    }

    clearThisTimeout() {
        if (this.timeout) {
            clearTimeout(this.timeout);
        }
    }
    // Calculate whether we need to expand the step due to linking
    expandAnchor(props) {
        const { node, location: { hash: anchorName } } = props;
        // e.g. #step-10-log-1
        if (anchorName) {
            const stepReg = /step-(.{1,})-log-.*/;
            const match = stepReg.exec(anchorName);
            if (match[1] && match[1] === node.id) {
                const isFocused = true;
                return { ...node, isFocused };
            }
        }
        return { ...node };
    }

    render() {
        const { logs, nodesBaseUrl, fetchLog, followAlong } = this.props;
        const node = this.expandAnchor(this.props);
        // Early out
        if (!node || !fetchLog) {
            return null;
        }
        const { config = {} } = this.context;
        const {
          isFocused = false,
          title,
          durationInMillis,
          result,
          id,
          state,
        } = node;

        const resultRun = result === 'UNKNOWN' || !result ? state : result;
        const log = logs ? logs[calculateLogUrl({ ...config, node, nodesBaseUrl })] : null;
        const getLogForNode = () => {
            // in case we do not have logs, or the logs are have no information attached we refetch them
            if (!log || !log.logArray) {
                fetchLog({ ...config, node, nodesBaseUrl });
            }
        };
        const runResult = resultRun.toLowerCase();
        const scrollToBottom =
            resultRun.toLowerCase() === 'failure'
            || (resultRun.toLowerCase() === 'running' && followAlong)
        ;
        return (<div>
            <ResultItem
              key={id}
              result={runResult}
              expanded={isFocused}
              label={title}
              onExpand={getLogForNode}
              durationMillis={durationInMillis}
            >
                { log && <LogConsole
                  key={id}
                  logArray={log.logArray}
                  scrollToBottom={scrollToBottom}
                  prefix={`step-${id}-`}
                /> } &nbsp;
            </ResultItem>
      </div>);
    }
}

Node.propTypes = {
    node: object.isRequired,
    followAlong: bool,
    logs: object,
    location: object,
    fetchLog: func,
    nodesBaseUrl: string,
};
