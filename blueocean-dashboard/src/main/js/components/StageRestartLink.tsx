import * as React from 'react';
import { Security, ToastUtils, RunApi as runApi } from '@jenkins-cd/blueocean-core-js';

interface Props {
    title: string;
    run: object;
    nodeRestartId: number;
    pipeline: Pipeline;
    onNavigation: Function;
    t: Function;
}

interface State {
    replaying: boolean;
}

interface Pipeline {
    name: string;
}

const RestartStageIcon = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 24C5.376 24 0 18.624 0 12C0 5.376 5.376 0 12 0C18.624 0 24 5.376 24 12C24 18.624 18.624 24 12 24Z" fill="#1D7DCF" />
        <path
            d="M7.04718 1.1875C7.04718 1.36328 7.01123 1.53088 6.94604 1.68408C7.4126 1.56409 7.90411 1.5 8.41248 1.5C11.5451 1.5 14.0337 3.93408 14.0337 6.875C14.0337 8.78528 12.9837 10.4817 11.3892 11.4362C11.787 11.6655 12.0532 12.0841 12.0532 12.5625C12.0532 12.6423 12.0458 12.7203 12.0316 12.7963C14.121 11.6042 15.5337 9.40967 15.5337 6.875C15.5337 3.05042 12.3173 0 8.41248 0C7.7901 0 7.1853 0.0775146 6.60815 0.223267C6.87811 0.463013 7.04718 0.806274 7.04718 1.1875Z"
            transform="translate(3.5 5.25)"
            fill="white"
        />
        <path
            d="M9.86127 13.6072C9.39288 13.7008 8.9082 13.75 8.41248 13.75C4.50763 13.75 1.2912 10.6996 1.2912 6.875C1.2912 4.5376 2.49255 2.48938 4.31818 1.24988C4.34503 1.80127 4.72571 2.26367 5.24689 2.43201C3.75885 3.40442 2.7912 5.04114 2.7912 6.875C2.7912 9.81592 5.27979 12.25 8.41248 12.25C8.74451 12.25 9.06927 12.2227 9.38464 12.1703C9.34436 12.2942 9.32263 12.4259 9.32263 12.5625C9.32263 12.9886 9.53394 13.3674 9.86127 13.6072Z"
            transform="translate(3.5 5.25)"
            fill="white"
        />
        <path
            d="M12 8C12 7.58582 11.6642 7.25 11.25 7.25C10.8358 7.25 10.5 7.58582 10.5 8V12.5C10.5 12.9142 10.8358 13.25 11.25 13.25H15.75C16.1642 13.25 16.5 12.9142 16.5 12.5C16.5 12.0858 16.1642 11.75 15.75 11.75H12V8Z"
            transform="translate(3.5 5.25)"
            fill="white"
        />
        <path
            d="M6 1.5C6 1.08582 5.66418 0.75 5.25 0.75H0.75C0.335815 0.75 0 1.08582 0 1.5C0 1.91418 0.335815 2.25 0.75 2.25H4.5V6C4.5 6.41418 4.83582 6.75 5.25 6.75C5.66418 6.75 6 6.41418 6 6V1.5Z"
            transform="translate(3.5 5.25)"
            fill="white"
        />
    </svg>
);

class StageRestartLink extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            replaying: false,
        };
    }

    _onRestartStageClick() {
        if (this.state.replaying) {
            return;
        }

        this.setState({
            replaying: true,
        });

        //change run to latest Run
        runApi
            .restartStage(this.props.run, this.props.nodeRestartId)
            .then(run => ToastUtils.createRunStartedToast(this.props.pipeline, run, this.props.onNavigation))
            .then(runDetailsUrl => this._afterReplayStarted(runDetailsUrl));
    }

    _afterReplayStarted(runDetailsUrl) {
        this.props.onNavigation(runDetailsUrl);
    }

    render() {
        if (Security.permit(this.props.pipeline).start()) {
            return (
                <a className="restart-stage" onClick={() => this._onRestartStageClick()}>
                    <RestartStageIcon />
                    <span>{this.props.t('rundetail.logToolbar.restartStage', { 0: this.props.title })}</span>
                </a>
            );
        } else {
            return false;
        }
    }
}

export default StageRestartLink;
