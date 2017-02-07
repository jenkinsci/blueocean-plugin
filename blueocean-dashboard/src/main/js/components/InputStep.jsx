import React, { Component, PropTypes } from 'react';
import {
    i18nTranslator,
    ParameterService,
    ParametersRender,
    ParameterApi as parameterApi,
    StringUtil,
} from '@jenkins-cd/blueocean-core-js';
/**
 * Simple helper to stop stopPropagation
 * @param event the event we want to cancel
 */
const stopProp = (event) => {
    event.stopPropagation();
};

/**
 * Translate function
 */
const translate = i18nTranslator('blueocean-dashboard');

/**
 * Creating a "<form/>"less form to submit the input parameters requested by the user in pipeline.
 *
 * We keep all form data in state and change them onChange and onToggle (depending of the parameter
 * type). We match the different supported inputTypes with a mapping functions
 * @see supportedInputTypesMapping
 * That mapping delegates to the specific implementation where we further delegate to JDL components.
 * In case you want to register a new mapping you need to edit './parameter/index' to add a new mapping
 * and further in './parameter/commonProptypes' you need to include the new type in the oneOf array.
 */
export default class InputStep extends Component {

    constructor(props) {
        super(props);
        this.parameterService = new ParameterService();
        this.parameterService.init(this.props.node.input.parameters);
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
     * Create a replica of the input parameters in state. Basically we just dump the whole item.
     * @param props
     */
    createFormState(props) {
        const { node } = props;
        // console.log({ node });
        if (node) {
            const { config = {} } = this.context;
            const {
                input: { id },
                _links: { self: { href } },
            } = node;
            this.setState({
                id,
                href: `${config._rootURL}${href}`,
                visible: false,
            });
        }
    }


    /**
     * Submit the form as "cancel" out of the state data id.
     */
    cancelForm() {
        const { href, id } = this.state;
        parameterApi.cancelInputParameter(href, id);
    }

    /**
     * Submit the form as "ok" out of the state data parameters and id.
     */
    okForm() {
        const { href, id } = this.state;
        const parameters = this.parameterService.parametersToSubmitArray();
        parameterApi.submitInputParameter(href, id, parameters);
    }

    render() {
        const { parameters } = this.parameterService;
        // Early out
        if (!parameters) {
            return null;
        }
        const { input: { message, ok } } = this.props.node;
        const cancelCaption = translate('rundetail.input.cancel', { defaultValue: 'Cancel' });
        const cancelButton = (<button title={cancelCaption} onClick={() => this.cancelForm()} className="btn btn-secondary inputStepCancel" >
            <span className="button-label">{cancelCaption}</span>
        </button>);

        return (<div className="inputStep">
            <div className="inputBody">
                <h3>{StringUtil.removeMarkupTags(message)}</h3>
                <ParametersRender
                  parameters={parameters}
                  onChange={(index, newValue) => this.parameterService.changeParameter(index, newValue) }
                />
                <div onClick={(event => stopProp(event))} className="inputControl">
                    { cancelButton }
                    <button title={ok} onClick={() => this.okForm()} className="btn inputStepSubmit" >
                        <span className="button-label">{ok}</span>
                    </button>
                </div>
            </div>
        </div>);
    }
}

const { object, shape } = PropTypes;

InputStep.propTypes = {
    node: shape().isRequired,
};

InputStep.contextTypes = {
    config: object.isRequired,
};
