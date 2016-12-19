import React, { PropTypes } from 'react';
import { supportedInputTypes } from './index';

const { oneOf, shape, string } = PropTypes;

export const propTypes = {
    parameter: shape({
        defaultParameterValue: shape({
            name: string,
            value: string,
        }),
        description: string,
        name: string,
        type: oneOf(supportedInputTypes),
    }),
};
