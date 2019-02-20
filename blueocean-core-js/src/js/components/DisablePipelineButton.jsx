import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { DisableJobApi as disableJobApi } from '../';
import { stopProp } from '../utils';
import { i18nTranslator } from '../i18n/i18n';

const translate = i18nTranslator('blueocean-web');

/**
 * Disable a pipeline
 */
export class DisablePipelineButton extends Component {
    constructor(props) {
        super(props);

        this.state = { disabled: this.props.pipeline.disabled };
    }

    _updateState(newDisabledState) {
        this.setState({
            disabled: newDisabledState,
        });
    }

    _onDisableClick() {
        disableJobApi.disable(this.props.pipeline).then(this._updateState(true));
    }

    _onEnableClick() {
        disableJobApi.enable(this.props.pipeline).then(this._updateState(false));
    }

    render() {
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');

        let buttonLabel;

        if (this.state.disabled) {
            buttonLabel = translate('enable.job', { defaultValue: 'Enable Job' });
        } else {
            buttonLabel = translate('disable.job', { defaultValue: 'Disable Job' });
        }

        const onClick = () => {
            this.state.disabled ? this._onEnableClick() : this._onDisableClick();
        };

        return (
            <div className="disable-job-button" onClick={event => stopProp(event)}>
                <a className={`${this.props.innerButtonClasses}`} title={buttonLabel} onClick={onClick}>
                    <Icon size={24} icon="ContentClear" style={{ marginRight: '5px' }} />
                    <span className="button-label">{buttonLabel}</span>
                </a>
            </div>
        );
    }
}

DisablePipelineButton.propTypes = {
    className: PropTypes.string,
    pipeline: PropTypes.object,
    onClick: PropTypes.func,
    buttonText: PropTypes.string,
    innerButtonClasses: PropTypes.string,
};

DisablePipelineButton.defaultProps = {
    innerButtonClasses: 'btn inverse',
};
