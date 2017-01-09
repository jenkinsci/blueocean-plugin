import React, { PropTypes } from 'react';

export function FormElement(props) {
    const extraClass = props.className || '';
    const errorClass = props.errorMessage ? 'u-error-state' : '';
    const dividerClass = props.showDivider ? 'u-show-divider' : '';

    return (
        <div className={`FormElement ${extraClass} ${errorClass} ${dividerClass}`}>
            <div className="FormElement-heading">
                <label className="FormElement-title">{props.title}</label>
                <span>&nbsp;</span>
                { props.errorMessage &&
                <span className="FormElement-error">- {props.errorMessage}</span>
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
    showDivider: PropTypes.bool,
};
