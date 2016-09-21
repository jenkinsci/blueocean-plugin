/**
 * Created by cmeyers on 8/26/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

import { RunApi as runApi } from '../';
import { ToastService as toastService } from '../';
import { ToastUtils } from '../';

const stopProp = (event) => {
    event.stopPropagation();
};

/**
 * Run Buttons allows a pipeline or branch to be run and also be stopped thereafter.
 */
export class RunButton extends Component {

    constructor(props) {
        super(props);

        this.state = {
            running: false,
            stopping: false,
        };
    }

    componentWillReceiveProps(nextProps) {
        this._updateState(nextProps);
    }

    _updateState(nextProps) {
        const oldStatus = this.props.latestRun && this.props.latestRun.state || '';
        const newStatus = nextProps.latestRun && nextProps.latestRun.state || '';

        // if the state of the run changed, then assume it's no longer trying to stop
        if (oldStatus !== newStatus) {
            this.setState({
                stopping: false,
            });
        }
    }

    _onRunClick() {
        runApi.startRun(this.props.runnable)
            .then((runInfo) => ToastUtils.createRunStartedToast(this.props.runnable, runInfo, this.props.onNavigation));
    }

    _onStopClick() {
        if (this.state.stopping) {
            return;
        }

        this.setState({
            stopping: true,
        });

        if (this.props.latestRun.state === 'QUEUED') {
            runApi.removeFromQueue(this.props.latestRun);
        } else {
            runApi.stopRun(this.props.latestRun);
        }

        const name = decodeURIComponent(this.props.runnable.name);
        const runId = this.props.latestRun.id;

        toastService.newToast({
            text: `Stopping "${name}" #${runId}...`,
        });
    }

    render() {
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? 'btn inverse' : '';
        const stopClass = this.state.stopping ? 'stopping' : '';

        const status = this.props.latestRun ? this.props.latestRun.state : '';
        const runningStatus = status && (status.toLowerCase() === 'running' || status.toLowerCase() === 'queued');

        const showRunButton = this.props.buttonType === 'run-only' ||
            (this.props.buttonType === 'toggle' && !runningStatus);
        const showStopButton = runningStatus && (this.props.buttonType === 'toggle' || this.props.buttonType === 'stop-only');

        const runLabel = this.props.runText || 'Run';
        const stopLabel = this.state.stopping ? 'Stopping...' : 'Stop';

        return (
            <div className={`run-button-component ${outerClass}`} onClick={(event => stopProp(event))}>
                { showRunButton &&
                <a className={`run-button ${innerButtonClass}`} title={runLabel} onClick={() => this._onRunClick()}>
                    <Icon size={24} icon="play_circle_outline" />
                    <span className="button-label">{runLabel}</span>
                </a>
                }

                { showStopButton &&
                <a className={`stop-button ${innerButtonClass} ${stopClass}`} title={stopLabel} onClick={() => this._onStopClick()}>
                    <div className="btn-icon"></div>
                    <span className="button-label">{stopLabel}</span>
                </a>
                }
            </div>
        );
    }
}

RunButton.propTypes = {
    buttonType: PropTypes.oneOf('toggle', 'stop-only', 'run-only'),
    className: PropTypes.string,
    runnable: PropTypes.object,
    latestRun: PropTypes.object,
    onNavigation: PropTypes.func,
    runText: PropTypes.string,
};

RunButton.defaultProps = {
    buttonType: 'toggle',
};
