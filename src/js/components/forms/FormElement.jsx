/**
 * Created by cmeyers on 11/1/16.
 */
import React, { PropTypes } from 'react';

export default function FormElement(props) {
    const extraClass = props.className || '';
    const errorClass = props.errorMessage ? 'error-state' : '';

    return (
        <div className={`form-element ${extraClass} ${errorClass}`}>
            { props.children }

            { props.errorMessage &&
            <div className="error-section">
                <span className="error-text">{props.errorMessage}</span>
            </div>
            }
        </div>
    );
}

FormElement.propTypes = {
    children: PropTypes.element,
    className: PropTypes.string,
    errorMessage: PropTypes.string,
};
