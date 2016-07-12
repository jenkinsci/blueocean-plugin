import React, { Component, PropTypes } from 'react';
import { ResultItem } from '@jenkins-cd/design-language';
import { calculateLogUrl } from '../util/UrlUtils';

import LogConsole from './LogConsole';

const { object, func, string } = PropTypes;

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
        const { logs, nodesBaseUrl, fetchLog } = nextProps;
        const { config = {} } = this.context;
        const node = this.expandAnchor(nextProps);
        const mergedConfig = { ...config, node, nodesBaseUrl };
        if (logs !== this.props.logs) {
            const key = calculateLogUrl(mergedConfig);
            const log = logs ? logs[key] : null;
            if (log && log !== null) {
                const number = Number(log.newStart);
                if (number > 0) {
                    mergedConfig.newStart = log.newStart;
                    // kill current  timeout if any
                    clearTimeout(this.timeout);
                    this.timeout = setTimeout(() => fetchLog(mergedConfig), 1000);
                }
            }
        }
    }

    componentWillUnmount() {
        clearTimeout(this.timeout);
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
        const { logs, nodesBaseUrl, fetchLog } = this.props;
        const node = this.expandAnchor(this.props);
        // Early out
        if (!node || !fetchLog) {
            return null;
        }
        const { override } = this.state;
        const { config = {} } = this.context;
        const {
          title,
          durationInMillis,
          result,
          id,
          isFocused,
          state,
        } = node;

        const resultRun = result === 'UNKNOWN' || !result ? state : result;
        const log = logs ? logs[calculateLogUrl({ ...config, node, nodesBaseUrl })] : null;
        const getLogForNode = () => {
            if (!log) {
                fetchLog({ ...config, node, nodesBaseUrl });
            }
        };
        const runResult = resultRun.toLowerCase();
        const scrollToBottom = runResult === 'failure' || runResult === 'running';
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
    logs: object,
    location: object,
    fetchLog: func,
    nodesBaseUrl: string,
};
