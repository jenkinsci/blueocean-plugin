import { Boolean } from './Boolean';
import { Choice } from './Choice';
import { String } from './String';
import { Text } from './Text';
import { Password } from './Password';
export { ParameterService } from './ParameterService';
export { ParametersRender } from './ParametersRender';
import { ParameterApi } from './rest/ParameterApi';

/**
 * all input types that we know of mapping against the component
 * @type {{BooleanParameterDefinition: Boolean, ChoiceParameterDefinition: Choice, TextParameterDefinition: String, StringParameterDefinition: Text, PasswordParameterDefinition: Password}}
 */
export const supportedInputTypesMapping =
    {
        BooleanParameterDefinition: Boolean,
        ChoiceParameterDefinition: Choice,
        TextParameterDefinition: Text,
        StringParameterDefinition: String,
        PasswordParameterDefinition: Password,
    };
/**
 * all input types that we know of
 * @type {Array}
 */
export const supportedInputTypes = Object.keys(supportedInputTypesMapping);

const parameterApi = new ParameterApi();
export { parameterApi as ParameterApi };
