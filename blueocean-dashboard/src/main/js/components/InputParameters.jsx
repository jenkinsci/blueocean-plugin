import React, { Component, PropTypes } from 'react';
import isoFetch from 'isomorphic-fetch';
import {
  i18nTranslator,
  ToastUtils,
  RunButton,
} from '@jenkins-cd/blueocean-core-js';
import { Dialog } from '@jenkins-cd/design-language';

import { supportedInputTypesMapping, ParameterService } from './parameter/index';

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
export default class InputParameters extends Component {

    constructor(props) {
        super(props);
        const { parameters = [] } = props.input;
        this.parameterService = new ParameterService();
        this.parameterService.addParameters(parameters);
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
     * Generic submit function. The calculations for the url has been done in
     * @see createFormState Here we simply POST the data to the server.
     * @param body - could be ok or cancel body
     */
    submitForm(body) {
        const { href } = this.state;
        const fetchOptions = {
            credentials: 'include',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        };
        return isoFetch(href, fetchOptions)
            .then(
                response => {
                    if (response.status >= 300 || response.status < 200) {
                        const error = new Error(response.statusText);
                        error.response = response;
                        throw error;
                    }
                    return response.json();
                }
            );
    }

    /**
     * Hide the dialog / Submit the form as "cancel"
     */
    hide() {
        this.setState({ visible: false });
    }
    /**
     * Submit the form out of the data parameters and create a Toast
     */
    initializeBuild() {
        const body = { parameters: this.parameterService.parametersToSubmitArray() };
        this.submitForm(body)
            .then((runInfo) => {
                ToastUtils
                  .createRunStartedToast(this.props.runnable, runInfo, this.props.onNavigation);});
        return this.hide();
    }

    /**
     * Show the dialog
     */
    show() {
        this.setState({ visible: true });
    }

    render() {
        const { runnable, onNavigation, latestRun } = this.props;
        const parameters = this.parameterService.parameters;
        const message = t('parameterized.pipeline.header', { defaultValue: 'Pipeline parameter' });
        const ok = t('parameterized.pipeline.submit', { defaultValue: 'Build' });
        const cancelCaption = t('rundetail.input.cancel');
        const cancelButton = (<button title={cancelCaption} onClick={() => this.hide()} className="btn inputStepCancel run-button btn-secondary" >
            <span className="button-label">{cancelCaption}</span>
        </button>);
        const runButtonProps = {
            buttonType: 'run-only',
            innerButtonClasses: 'btn-secondary',
            runnable,
            onNavigation,
            latestRun,
        };
        if (parameters.length > 0) {
            runButtonProps.onClick = () => {
                this.show();
            };
        }
        const buttons = [cancelButton,
            <button title={ok} onClick={() => this.initializeBuild()} className="btn inputStepSubmit" >
                <span className="button-label">{ok}</span>
        </button>];
        return (<div>
            <RunButton {...runButtonProps} />
            { this.state.visible && <Dialog
              buttons={buttons}
              onDismiss={() => console.log('User dismiss')}
              title={message}
              className="Dialog--input"
            >
                {
                    parameters.map((parameter, index) => {
                        const { type } = parameter;
                        const returnValue = supportedInputTypesMapping[type];
                        if (returnValue) {
                            return React.createElement(returnValue, {
                                ...parameter,
                                key: index,
                                onChange: (event) => this.parameterService.changeParameter(index, event),
                            });
                        }
                        return <div>No component found for type {type}.</div>;
                    })
                }
            </Dialog>}
        </div>);
    }
}

const { bool, func, object } = PropTypes;

InputParameters.propTypes = {
    input: object,
    visible: bool,
    onNavigation: func,
    runnable: object,
    latestRun: object,
};

InputParameters.contextTypes = {
    config: object.isRequired,
};
