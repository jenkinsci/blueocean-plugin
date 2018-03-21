import React from 'react';
import { Alerts } from '@jenkins-cd/design-language';
import i18nTranslator from '../../i18n/i18n';
import { supportedInputTypesMapping } from '../index';

/**
 * Translate function
 */
const translate = i18nTranslator('blueocean-web');

export function ParametersRender(properties) {
    const { parameters, onChange = () => {} } = properties;
    const renderedParameters = parameters.map((parameter, index) => {
        const { type } = parameter;
        const returnValue = supportedInputTypesMapping[type];
        if (returnValue) {
            return React.createElement(returnValue, {
                ...parameter,
                key: index,
                onChange: onChange.bind(this, index),
            });
        }
        // error case - returning an error alert
        const alertCaption = translate('parameter.error.message', { 0: type });
        const alertTitle = translate('parameter.error.title');
        return <Alerts message={alertCaption} type="Error" title={alertTitle} />;
    });

    return <div>{renderedParameters}</div>;
}
