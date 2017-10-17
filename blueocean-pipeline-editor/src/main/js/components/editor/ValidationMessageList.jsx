import React from 'react';
import pipelineValidator from '../../services/PipelineValidator';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

export function ValidationMessageList({node, errors}) {
    const nodeErrors = errors ? errors : (node && pipelineValidator.getNodeValidationErrors(node));
    return (
        <ReactCSSTransitionGroup component="div" className="pipeline-validation-errors"
            transitionName="collapse-height"
            transitionEnter
            transitionLeave
            transitionEnterTimeout={300}
            transitionLeaveTimeout={400}
        >
            {nodeErrors &&
                <div className="collapse-height">
                    <ul className="error-list">
                        {nodeErrors.map(err =>
                            <li>{err.error ? err.error : err}</li>
                        )}
                    </ul>
                </div>
            }
        </ReactCSSTransitionGroup>
    );
}
