import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { DisableJobApi as disableJobApi } from '../';
import { Security } from '../security';
import { stopProp } from '../utils';

const { permit } = Security;

/**
 * Disable a pipeline
 */
export class DisablePipelineButton extends Component {
    constructor(props) {
        super(props);

        this.state = {
            disabled: this.props.pipeline.disabled,
            submittingChange: false,
        };
    }

    _updateState(newDisabledState) {
        this.setState({
            disabled: newDisabledState,
        });
    }

    _onDisableClick() {
        disableJobApi.disable(this.props.pipeline).then(() => {
            this._updateState(true);
            this.props.onChangeDisableState(true);
            this.setState({ submittingChange: false });
        });
    }

    _onEnableClick() {
        disableJobApi.enable(this.props.pipeline).then(() => {
            this._updateState(false);
            this.props.onChangeDisableState(false);
            this.setState({ submittingChange: false });
        });
    }

    render() {
        const { t, pipeline, innerButtonClasses } = this.props;

        //if the user doesn't have config permission, don't show the button
        if (!permit(pipeline).configure()) {
            return false;
        }

        const buttonDisabled = this.state.submittingChange ? true : false;

        let buttonLabel;
        let buttonIcon;

        if (this.state.disabled) {
            buttonLabel = t('pipelinedetail.activity.header.enable.job', { defaultValue: 'Enable Job' });
            buttonIcon = 'ActionCheckCircleOutline';
        } else {
            buttonLabel = t('pipelinedetail.activity.header.disable.job', { defaultValue: 'Disable Job' });
            buttonIcon = 'AvNotInterested';
        }

        const onClick = () => {
            if (!this.state.submittingChange) {
                this.setState({ submittingChange: true });
                this.state.disabled ? this._onEnableClick() : this._onDisableClick();
            }
        };

        return (
            <div className="disable-job-button" onClick={event => stopProp(event)}>
                <a className={`${innerButtonClasses}`} title={buttonLabel} onClick={onClick} disabled={buttonDisabled}>
                    <Icon size={24} icon={buttonIcon} style={{ marginRight: '5px' }} />
                    <span className="button-label">{buttonLabel}</span>
                </a>
            </div>
        );
    }
}

DisablePipelineButton.propTypes = {
    t: PropTypes.func,
    pipeline: PropTypes.object,
    onClick: PropTypes.func,
    onChangeDisableState: PropTypes.func,
    buttonText: PropTypes.string,
    innerButtonClasses: PropTypes.string,
};

DisablePipelineButton.defaultProps = {
    innerButtonClasses: 'btn inverse',
};
