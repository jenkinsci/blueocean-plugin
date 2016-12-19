import React, { PropTypes } from 'react';

const { oneOf, shape, string } = PropTypes;

export const propTypes = {
    parameter: shape({
        defaultParameterValue: shape({
            name: string,
            value: string,
        }),
        description: string,
        name: string,
        type: oneOf([
            'BooleanParameterDefinition',
            'ChoiceParameterDefinition',
            'TextParameterDefinition',
            'StringParameterDefinition',
            'PasswordParameterDefinition',
        ]),
    }),
};
