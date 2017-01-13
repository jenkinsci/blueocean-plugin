import React, { Component, PropTypes } from 'react';

import {
  i18nTranslator,
  ToastUtils,
  RunButton,
} from '@jenkins-cd/blueocean-core-js';

import {
    ParameterService,
    ParametersRender,
    ParameterApi as parameterApi,
} from './index';

import { Dialog } from '@jenkins-cd/design-language';
/**
 * Translate function
 */
const t = i18nTranslator('blueocean-dashboard');

/**
 * Creating a "<form/>"less form to submit the build parameters requested by the user for a parametrised job..
 *
 * We keep all form data in the ParameterService and change them onChange and onToggle (depending of the parameter
 * type). We match the different supported inputTypes with a mapping functions
 * @see supportedInputTypesMapping
 * That mapping delegates to the specific implementation where we further delegate to JDL components.
 * In case you want to register a new mapping you need to edit './parameter/index' to add a new mapping
 * and further in './parameter/commonProptypes' you need to include the new type in the oneOf array.
 */
export class ParametersRunButton extends Component {

    constructor(props) {
        super(props);
        if (props.input && props.input.parameters) {
            const { parameters } = props.input;
            this.parameterService = new ParameterService();
            this.parameterService.init(parameters);
        } else {
            this.parameterService = { parameters: [] };
        }
    }
    // we start with an empty state
    state = {};
    /**
     * react life cycle mapper to invoke the creation of the form state
     */
    componentWillMount() {
        this.createFormState(this.props);
    }
    /**
     * Create some information for form handling
     * @param props
     */
    createFormState(props) {
        const { input } = props;
        if (input) {
            const { config = {} } = this.context;
            const { _links: { self: { href } } } = input;
            this.setState({
                href: `${config._rootURL}${href}/runs/`,
                visible: false,
            });
        }
    }
    /**
     * Hide the dialog / Submit the form as "cancel"
     */
    hide() {
        this.setState({ visible: false });
    }
    /**
     * Show the dialog
     */
    show() {
        this.setState({ visible: true });
    }
    /**
     * Submit the form out of the data parameters and create a Toast
     */
    initializeBuild() {
        const parameters = this.parameterService.parametersToSubmitArray();
        parameterApi.startRunWithParameters(this.state.href, parameters)
            .then((runInfo) => {
                ToastUtils
                  .createRunStartedToast(this.props.runnable, runInfo, this.props.onNavigation);
            });
        return this.hide();
    }

    render() {
        const { parameters } = this.parameterService;
        // Captions
        const message = t('parametrised.pipeline.header', { defaultValue: 'Input required' });
        const ok = t('parametrised.pipeline.submit', { defaultValue: 'Build' });
        const cancelCaption = t('parametrised.pipeline.cancel', { defaultValue: 'Cancel' });
        // buttons
        const cancelButton = (<button title={cancelCaption} onClick={() => this.hide()} className="btn inputStepCancel run-button btn-secondary" >
            <span className="button-label">{cancelCaption}</span>
        </button>);
        const okButton = (<button title={ok} onClick={() => this.initializeBuild()} className="btn inputStepSubmit" >
            <span className="button-label">{ok}</span>
        </button>);
        // common run properties
        const runButtonProps = { ...this.props };
        // when we have build parameters we need to show them before trigger a build
        if (parameters.length > 0) {
            runButtonProps.onClick = () => {
                this.show();
            };
        }
        return (<div>
            <RunButton {...runButtonProps} />
            { this.state.visible &&
                <div className="inputParameters">
                    <Dialog
                      buttons={[cancelButton, okButton]}
                      onDismiss={this.hide.bind(this)}
                      title={message}
                      className="Dialog--input"
                    >
                        <ParametersRender
                          parameters={parameters}
                          onChange={(index, newValue) => this.parameterService.changeParameter(index, newValue) }
                        />
                    </Dialog>
                </div>
            }
        </div>);
    }
}

const { bool, func, object, oneOf, string } = PropTypes;

ParametersRunButton.propTypes = {
    input: object,
    visible: bool,
    onNavigation: func,
    runnable: object,
    latestRun: object,
    buttonType: oneOf('toggle', 'stop-only', 'run-only'),
    className: string,
    onClick: func,
    runText: string,
    innerButtonClasses: string,
};

ParametersRunButton.contextTypes = {
    config: object.isRequired,
};
