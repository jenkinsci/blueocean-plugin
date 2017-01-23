import React from 'react';
import { supportedInputTypesMapping } from '../index';

export function ParametersRender(properties) {
    const { parameters, onChange = () => {} } = properties;
    return (<div>
        {
            parameters.map((parameter, index) => {
                const { type } = parameter;
                const returnValue = supportedInputTypesMapping[type];
                if (returnValue) {
                    return React.createElement(returnValue, {
                        ...parameter,
                        key: index,
                        onChange: onChange.bind(this, index),
                    });
                }
                return <div>No component found for type {type}.</div>;
            })
        }
    </div>);
}
