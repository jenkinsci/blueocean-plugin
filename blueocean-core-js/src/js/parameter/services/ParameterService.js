import { action, observable, computed } from 'mobx';

/**
 * Holds one or more parameters in state for display in UI.
 */
export class ParameterService {

    @observable parameters = [];

    /**
     * initialize array from parameters.
     * @param parameters - array of parameter
     */
    @action
    init(parameters) {
        this.parameters = parameters;
    }
    /**
     * Adds a parameter to the list.
     *
     * @param parameter object with the following shape:
     * {
     * "_class": "hudson.model.BooleanParameterDefinition",
     * "defaultParameterValue": {
     *    "_class": "hudson.model.BooleanParameterValue",
     *    "name": "isFoo",
     *    "value": false,
     *    "_capabilities": ["hudson.model.ParameterValue"]
     * },
     * "description": "isFoo should be false",
     * "name": "isFoo",
     * "type": "BooleanParameterDefinition",
     * "_capabilities": ["hudson.model.SimpleParameterDefinition", "hudson.model.ParameterDefinition"]
     * }
     */
    @action
    addParameter(parameter) {
        this.parameters.push(parameter);
    }

    /**
     * Add an array of parameters.
     * @param parameters - array of parameter
     */
    @action
    addParameters(parameters) {
        this.parameters.push.apply(this.parameters, parameters);
    }

    /**
     * Change the defaultValue of an existing parameter
     * @param index - the position of the object
     * @param newValue - the value of the user selection becomes the ne default parameter
     */
    @action
    changeParameter(index, newValue) {
        this.parameters[index].defaultParameterValue.value = newValue;
    }

    /**
     * Creates an array from the parameter array which is in the current state
     * @returns {array}
     */
    parametersToSubmitArray() {
        return this.parameters.map(item => {
            const parameter = { name: item.name, value: item.defaultParameterValue.value };
            return parameter;
        });
    }

    @computed get count() {
        return this.parameters ? this.parameters.length : 0;
    }


}
