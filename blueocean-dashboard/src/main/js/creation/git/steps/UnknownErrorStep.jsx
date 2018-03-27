import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

const VERBOSE_ERRORS = true;

@observer
export class UnknownErrorStep extends React.Component {
    _renderErrorDetails(errors) {
        if (!VERBOSE_ERRORS) {
            return null;
        }

        return (
            <div>
                {errors &&
                    errors.map(err => (
                        <p className="instructions">
                            <div>Field: {err.field}</div>
                            <div>Code: {err.code}</div>
                            <div>Message: {err.message}</div>
                        </p>
                    ))}
            </div>
        );
    }

    render() {
        const { error } = this.props;
        const content = this._renderErrorDetails(error.errors);
        const title = 'Error';

        return (
            <FlowStep {...this.props} className="unknown-error-step" title={title} error>
                <p className="instructions">An unknown error has occurred. You may try again.</p>

                <p className="instructions">Message: {error.message}</p>

                {content}
            </FlowStep>
        );
    }
}

UnknownErrorStep.propTypes = {
    flowManager: PropTypes.object,
    error: PropTypes.object,
};
