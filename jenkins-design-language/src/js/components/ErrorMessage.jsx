import React from 'react';
import PropTypes from 'prop-types';

export function ErrorMessage(props) {
    const extraClass = props.className;
    return <div className={`ErrorMessage ${extraClass}`}>{props.children}</div>;
}

ErrorMessage.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
};

ErrorMessage.defaultProps = {
    className: '',
};
