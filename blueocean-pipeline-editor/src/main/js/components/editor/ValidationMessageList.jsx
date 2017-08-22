// @flow

import React from 'react';
import pipelineValidator from '../../services/PipelineValidator';

export function ValidationMessageList({node}) {
    if (!node) {
        return null;
    }
    const errors = pipelineValidator.getNodeValidationErrors(node);
    if (!errors || !errors.length) {
        return null;
    }
    
    return (<ul className="pipeline-validation-errors">
        {errors.map(err =>
            <li>{err.error ? err.error : err}</li>
        )}
    </ul>);
}
