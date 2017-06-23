/**
 * Created by cmeyers on 8/26/16.
 */
import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { RunApi as runApi, ToastService as toastService, ToastUtils } from '../';
import Security from '../security';
import { stopProp } from '../utils';
import i18nTranslator from '../i18n/i18n';

const translate = i18nTranslator('blueocean-web');

const { permit } = Security;

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
            .then((run) => ToastUtils.createRunStartedToast(this.props.runnable, run, this.props.onNavigation));
    }

    _onStopClick() {
        if (this.state.stopping) {
            return;
        }

        this.setState({
            stopping: true,
        });

        runApi.stopRun(this.props.latestRun);

        const name = decodeURIComponent(this.props.runnable.name);
        const runId = this.props.latestRun.id;
        const text = translate('toast.run.stopping', {
            0: name,
            1: runId,
            defaultValue: 'Stoppping "{0}" #{1}',
        });

        toastService.newToast({ text });
    }

    render() {
        const buttonType = this.props.buttonType;
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? this.props.innerButtonClasses : '';
        const stopClass = this.state.stopping ? 'stopping' : '';

        const status = this.props.latestRun ? this.props.latestRun.state : '';
        const isPaused = status.toLowerCase() === 'paused';
        const runningStatus = status && (isPaused || status.toLowerCase() === 'running' || status.toLowerCase() === 'queued');

        let showRunButton = buttonType === 'run-only' || (buttonType === 'toggle' && !runningStatus);
        let showStopButton = runningStatus && (buttonType === 'toggle' || buttonType === 'stop-only');

        showRunButton = showRunButton && permit(this.props.runnable).start();
        showStopButton = showStopButton && permit(this.props.runnable).stop();

        const runLabel = this.props.runText || translate('toast.run', {
            defaultValue: 'Run',
        });
        let stopLabel = this.state.stopping ? translate('toast.stopping', {
            defaultValue: 'Stopping ...',
        }) : translate('toast.stop', {
            defaultValue: 'Stop',
        });

        if (isPaused && !this.state.stopping) {
            stopLabel = translate('toast.abort', { defaultValue: 'Abort' });
        }

        if (!showRunButton && !showStopButton) {
            return null;
        }

        const { onClick = () => this._onRunClick() } = this.props;
        return (
            <div className={`run-button-component ${outerClass}`} onClick={(event => stopProp(event))}>
                { showRunButton &&
                <a className={`run-button ${innerButtonClass}`} title={runLabel} onClick={onClick}>
                    <Icon size={24} icon="play_circle_outline" />
                    <span className="button-label">{runLabel}</span>
                </a>
                }

                { showStopButton &&
                <a className={`stop-button ${innerButtonClass} ${stopClass}`} title={stopLabel} onClick={() => this._onStopClick()}>
                    { /* eslint-disable max-len */ }
                    <svg className="svg-icon" width="20" height="20" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                        <g fill="none" fill-rule="evenodd">
                            <path d="M-2-2h24v24H-2z" />
                            <path className="svg-icon-inner" d="M10 0C4.48 0 0 4.48 0 10s4.48 10 10 10 10-4.48 10-10S15.52 0 10 0zM7 7h6v6H7V7zm3 11c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" fill="#4A90E2" />
                        </g>
                    </svg>
                    { /* eslint-enable max-len */ }
                    <span className="button-label">{stopLabel}</span>
                </a>
                }
            </div>
        );
    }
}

RunButton.propTypes = {
    buttonType: PropTypes.oneOf(['toggle', 'stop-only', 'run-only']),
    className: PropTypes.string,
    runnable: PropTypes.object,
    latestRun: PropTypes.object,
    onNavigation: PropTypes.func,
    onClick: PropTypes.func,
    runText: PropTypes.string,
    innerButtonClasses: PropTypes.string,
};

RunButton.defaultProps = {
    buttonType: 'toggle',
    innerButtonClasses: 'btn inverse',
};
