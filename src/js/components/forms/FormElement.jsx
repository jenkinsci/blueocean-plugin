/**
 * Created by cmeyers on 11/1/16.
 */
import React, { PropTypes } from 'react';

export function FormElement(props) {
    const extraClass = props.className || '';
    const errorClass = props.errorMessage ? 'error-state' : '';

    return (
        <div className={`form-element ${extraClass} ${errorClass}`}>
            <div className="form-heading">
                <label className="form-label">{props.title}</label>
                <span>&nbsp;</span>
                { props.errorMessage &&
                <span className="error-text">- {props.errorMessage}</span>
                }
            </div>

            { props.children }
        </div>
    );
}

FormElement.propTypes = {
    children: PropTypes.element,
    className: PropTypes.string,
    title: PropTypes.string,
    errorMessage: PropTypes.string,
};
