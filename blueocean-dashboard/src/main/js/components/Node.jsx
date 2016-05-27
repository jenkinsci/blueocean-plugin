import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';
import { StatusIndicator } from '@jenkins-cd/design-language';
import moment from 'moment';
import { calculateLogUrl } from '../redux';

import LogConsole from './LogConsole';

const { object, func, string } = PropTypes;

export default class Node extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: this.props.node.isFocused };
    }
    componentWillMount() {
        const { node, nodesBaseUrl, fetchLog } = this.props;
        const { config = {} } = this.context;
        if (node && node.isFocused) {
            const mergedConfig = { ...config, node, nodesBaseUrl };
            fetchLog(mergedConfig);
        }
    }

    render() {
        const { node, logs, nodesBaseUrl, fetchLog } = this.props;
        const { isVisible } = this.state;
        // Early out
        if (!node || !fetchLog) {
            return null;
        }
        const { config = {} } = this.context;
        const {
          key,
          title,
          durationInMillis,
          result,
          state,
        } = node;

        const resultRun = result === 'UNKNOWN' || !result ? state : result;
        const duration = moment.duration(
          Number(durationInMillis), 'milliseconds').humanize();
        const log = logs ? logs[calculateLogUrl({ ...config, node, nodesBaseUrl })] : null;

        const getLogForNode = () => {
            if (!log) {
                fetchLog({ ...config, node, nodesBaseUrl });
            }
            this.setState({ isVisible: !this.state.isVisible });
        };

        return (<div>
        <div className="nodes" key={key} onClick={getLogForNode}>
          <div className="nodes__section">
            <div className="result"><StatusIndicator result={resultRun} /></div>
            <div className="state">
              { this.state.isVisible ?
                <Icon {...{ icon: 'keyboard_arrow_down' }} /> :
                <Icon {...{ icon: 'keyboard_arrow_right' }} />}
            </div>
            <div className="title">{title}</div>
          </div>
          <div className="nodes__section">
            <div className="duration">{duration}</div>
          </div>
        </div>
        { log && isVisible && <LogConsole data={log.text} /> }
      </div>);
    }
}

Node.propTypes = {
    node: object.isRequired,
    logs: object,
    fetchLog: func,
    nodesBaseUrl: string,
};
