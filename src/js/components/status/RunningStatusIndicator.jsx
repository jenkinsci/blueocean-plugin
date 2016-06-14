// @flow

import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import { StatusIndicator, decodeResultValue } from './StatusIndicator';

const {number, string} = PropTypes;

const validResultValues = {
    success: 'success',
    failure: 'failure',
    running: 'running',
    queued: 'queued',
    unstable: 'unstable',
    aborted: 'aborted',
    not_built: 'not_built',
    unknown: 'unknown'
};

// Enum type from const validResultValues
//export type Result = $Keys<typeof validResultValues>;

export class RunningStatusIndicator extends Component {

    //static validResultValues:typeof validResultValues;

    constructor(props) {
        super(props);

        this.state = {
            percentage: 0,
        };

        this.runningStartMillis = - 1;
        this.clearIntervalId = -1;
    }

    componentDidMount() {
        this._initializeProgress(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initializeProgress(nextProps);
    }

    _initializeProgress(props) {
        if (!props) {
            return;
        }

        const cleanResult = decodeResultValue(props.result);
        const isRunning = cleanResult === validResultValues.running;

        if (isRunning) {
            this.runningStartMillis = this.props.startTime || moment().valueOf();

            this._updateProgress();

            this.clearIntervalId = setInterval(() => {
                this._updateProgress();
            }, 1000);
        }
        else
        {
            this._stopProgressUpdates();
        }
    }

    _updateProgress() {
        const nowMillis = moment().valueOf();
        const percentage = (nowMillis - this.runningStartMillis) / this.props.estimatedDuration * 100;
        this.setState({
            percentage
        });

        if (percentage >= 100) {
            this._stopProgressUpdates();
        }
    }

    _stopProgressUpdates() {
        clearInterval(this.clearIntervalId);
    }

    componentWillUnmount() {
        this._stopProgressUpdates();
    }

    render() {
        return (
            <StatusIndicator { ... this.props } percentage={this.state.percentage} />
        );
    }
}

RunningStatusIndicator.propTypes = {
    result: string,
    percentage: number,
    width: string,
    height: string,
    startTime: number,
    estimatedDuration: number,
};

RunningStatusIndicator.validResultValues = validResultValues;