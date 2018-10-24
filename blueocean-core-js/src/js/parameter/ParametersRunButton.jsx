import React, { Component, PropTypes } from 'react';
import { Alerts, Dialog } from '@jenkins-cd/design-language';

import { UrlBuilder, capable, RunButtonBase as RunButton, ToastUtils } from '../index';

import { i18nTranslator } from '../i18n/i18n';

import { ParameterApi as parameterApi, ParameterService, ParametersRender, supportedInputTypesMapping } from './index';

import { logging } from '../logging';
const logger = logging.logger('io.jenkins.blueocean.core.ParametersRunButton');

/**
 * Translate function
 */
const t = i18nTranslator('blueocean-web');
const MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';
/**
 * Creating a "<form/>"less form to submit the build parameters requested by the user for a parameterized job..
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
        if (props.runnable && props.runnable.parameters) {
            const { parameters } = props.runnable;
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
     * react life cycle mapper to invoke the update of the service
     */
    componentWillReceiveProps(nextProps) {
        if (
            nextProps.runnable &&
            nextProps.runnable.parameters &&
            this.props.runnable &&
            this.props.runnable.parameters &&
            nextProps.runnable.parameters !== this.props.runnable.parameters
        ) {
            this.parameterService.init(nextProps.runnable.parameters);
        }
    }
    /**
     * Create some information for form handling
     * @param props
     */
    createFormState(props) {
        const { runnable } = props;
        if (runnable) {
            const { config = {} } = this.context;
            const {
                _links: {
                    self: { href },
                },
            } = runnable;
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
        parameterApi.startRunWithParameters(this.state.href, parameters).then(run => {
            ToastUtils.createRunStartedToast(this.props.runnable, run, this.props.onNavigation);
        });
        return this.hide();
    }

    render() {
        const { parameters } = this.parameterService;

        // Captions
        const message = t('parameterized.pipeline.header', { defaultValue: 'Input required' });
        const ok = t('parameterized.pipeline.submit', { defaultValue: 'Run' });
        const cancelCaption = t('parameterized.pipeline.cancel', { defaultValue: 'Cancel' });
        // buttons
        const cancelButton = (
            <button title={cancelCaption} onClick={() => this.hide()} className="btn inputStepCancel run-button btn-secondary">
                <span className="button-label">{cancelCaption}</span>
            </button>
        );
        const okButton = (
            <button title={ok} onClick={() => this.initializeBuild()} className="btn inputStepSubmit">
                <span className="button-label">{ok}</span>
            </button>
        );
        // common run properties
        const runButtonProps = { ...this.props };
        // when we have build parameters we need to show them before trigger a build
        if (parameters.length > 0 && runButtonProps.buttonType !== 'stop-only') {
            runButtonProps.onClick = () => {
                this.show();
            };
        }
        const isMultiBranch = capable(this.props.runnable, MULTIBRANCH_PIPELINE);
        const pipe = { fullName: this.props.runnable.fullName };
        if (isMultiBranch) {
            pipe.fullName += `/${pipe.branch}`;
        }
        const classicBuildUrl = UrlBuilder.buildClassicBuildUrl(pipe);
        const sanity = parameters.filter(parameter => supportedInputTypesMapping[parameter.type] !== undefined);
        logger.debug('sane?', sanity.length === parameters.length, 'classicBuildUrl: ', classicBuildUrl);
        let dialog;
        if (sanity.length !== parameters.length) {
            logger.debug('sanity check failed. Returning Alert instead of the form.');
            const alertCaption = [
                <p>{t('inputParameter.error.message')}</p>,
                <a href={classicBuildUrl} target="_blank">
                    {t('inputParameter.error.linktext')}
                </a>,
            ];
            const alertTitle = t('inputParameter.error.title', { defaultValue: 'Error' });
            dialog = (
                <Dialog onDismiss={this.hide.bind(this)} title={message} className="Dialog--input">
                    <Alerts message={alertCaption} type="Error" title={alertTitle} />
                </Dialog>
            );
        } else {
            dialog = (
                <Dialog buttons={[okButton, cancelButton]} onDismiss={this.hide.bind(this)} title={message} className="Dialog--input Dialog--medium-size">
                    <ParametersRender parameters={parameters} onChange={(index, newValue) => this.parameterService.changeParameter(index, newValue)} />
                </Dialog>
            );
        }
        return (
            <div className="ParametersRunButton">
                <RunButton {...runButtonProps} />
                {this.state.visible && <div className="inputParameters">{dialog}</div>}
            </div>
        );
    }
}

const { bool, func, object, oneOf, string } = PropTypes;

ParametersRunButton.propTypes = {
    visible: bool,
    onNavigation: func,
    runnable: object,
    latestRun: object,
    buttonType: oneOf(['toggle', 'stop-only', 'run-only']),
    className: string,
    onClick: func,
    runText: string,
    innerButtonClasses: string,
};

ParametersRunButton.contextTypes = {
    config: object.isRequired,
};
