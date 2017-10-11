import React, { PropTypes } from 'react';
import { ErrorMessage } from '../ErrorMessage';

export function FormElement(props) {
    if (!props.title && !props.errorMessage && !props.children) {
        return null;
    }

    const extraClass = props.className || '';
    const errorClass = props.errorMessage ? 'u-error-state' : '';
    const dividerClass = props.showDivider ? 'u-show-divider' : '';
    const layoutClass = props.verticalLayout ? 'u-layout-vertical' : '';
    const childFreeClass = !props.children ? 'u-child-free' : '';

    return (
        <div className={`FormElement ${extraClass} ${errorClass} ${dividerClass} ${layoutClass} ${childFreeClass}`}>
            {(props.title || props.errorMessage) && (
                <div className="FormElement-heading">
                    {props.title && <label className="FormElement-title">{props.title}</label>}
                    {props.title && props.errorMessage && <span>&nbsp;-&nbsp;</span>}
                    {props.errorMessage && <ErrorMessage>{props.errorMessage}</ErrorMessage>}
                </div>
            )}
            {props.children && <div className="FormElement-children">{props.children}</div>}
        </div>
    );
}

FormElement.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    title: PropTypes.string,
    errorMessage: PropTypes.string,
    showDivider: PropTypes.bool,
    verticalLayout: PropTypes.bool,
};
