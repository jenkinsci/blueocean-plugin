// Implementations
import { Boolean } from './components/Boolean';
import { Choice } from './components/Choice';
import { String } from './components/String';
import { Text } from './components/Text';
import { Password } from './components/Password';
export { Boolean, Choice, String, Text, Password };
// Renderer
export { ParametersRender } from './renderer/ParametersRender';
export { DebugRender } from './renderer/DebugRender';
// service and apis
export { ParameterService } from './services/ParameterService';
import { ParameterApi } from './rest/ParameterApi';
// run button extension
export { ParametersRunButton } from './ParametersRunButton';
/**
 * all input types that we know of mapping against the component
 * @type {{BooleanParameterDefinition: Boolean, ChoiceParameterDefinition: Choice, TextParameterDefinition: String, StringParameterDefinition: Text, PasswordParameterDefinition: Password}}
 */
export const supportedInputTypesMapping = {
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
