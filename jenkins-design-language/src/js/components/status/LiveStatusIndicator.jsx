// @flow

import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { StatusIndicator, decodeResultValue } from './StatusIndicator';

import type Moment from 'moment';

type Props = {
    result: string,
    percentage?: number,
    width: string,
    height: string,
    noBackground: boolean,
    startTime: string,
    estimatedDuration: number,
};

type State = {
    percentage: number,
};

/**
 * LiveStatusIndicator is a wrapper around StatusIndicator that allows
 * for an in-progress status to self update.
 *
 * Properties:
 * "estimatedDuration": time in millis over which the progress indicator will update.
 * "startTime": ISO-8601 string indicating when tracking of progress begins from.
 */
export class LiveStatusIndicator extends Component {
    props: Props;
    state: State;

    percentage: number;
    startTime: ?Moment;
    clearIntervalId: number;
    animationFrameId: number;

    constructor(props: Props) {
        super(props);

        this.state = {
            // percentage of progress currently drawn in UI
            percentage: 0,
        };

        // percentage of progress based on last check
        this.percentage = 0;
        this.startTime = null;
        this.clearIntervalId = 0;
        this.animationFrameId = 0;
    }

    componentDidMount() {
        this._initializeProgress(this.props);
    }

    componentWillReceiveProps(nextProps: Props) {
        this._initializeProgress(nextProps);
    }

    _initializeProgress(props: Props) {
        // ensure we don't leak setInterval by proactively clearing it
        // the code will restart the interval if needed
        this._stopProgressUpdates();

        if (!props) {
            return;
        }

        const cleanResult = decodeResultValue(props.result);
        // TODO: pull in validResultValues from StatusIndicator
        const isRunning = cleanResult === 'running';

        if (isRunning) {
            this.startTime = moment(props.startTime, moment.ISO_8601).utcOffset(props.startTime);

            // update the progress each second
            this.clearIntervalId = setInterval(() => {
                this._updateProgress(this.props);
            }, 1000);

            this._updateProgress(props);
        }
    }

    _updateProgress(props: Props) {
        const now = moment();
        const elapsed = now.diff(this.startTime);
        const estimatedDuration = props.estimatedDuration;

        if (elapsed > 0 && estimatedDuration > 0) {
            this.percentage = Math.floor(elapsed / estimatedDuration * 100);
        } else {
            // if both aren't available, set to 'indeterminate' state
            this.percentage = 101;
        }

        if (0 <= this.percentage && this.percentage <= 100) {
            this._drawProgress();
        } else {
            // set the percentage > 100 so the indeterminate spinner will display
            // no more progress updates are required
            this.setState({
                percentage: 101,
            });

            this._stopProgressUpdates();
        }
    }

    _drawProgress() {
        if (this.state.percentage <= this.percentage) {
            // increment the progress to trigger a rerender
            // then request another draw on next frame
            const newPercent = this.state.percentage + 1;
            this.setState({
                percentage: newPercent,
            });

            this.animationFrameId = requestAnimationFrame(() => {
                this._drawProgress();
            });
        }
    }

    _stopProgressUpdates() {
        clearInterval(this.clearIntervalId);
        this.clearIntervalId = 0;
        cancelAnimationFrame(this.animationFrameId);
        this.animationFrameId = 0;
    }

    componentWillUnmount() {
        this._stopProgressUpdates();
    }

    render() {
        return <StatusIndicator {...this.props} percentage={this.state.percentage} />;
    }
}

LiveStatusIndicator.propTypes = {
    result: PropTypes.string,
    percentage: PropTypes.number,
    width: PropTypes.string,
    height: PropTypes.string,
    noBackground: PropTypes.bool,
    startTime: PropTypes.string,
    estimatedDuration: PropTypes.number,
};
