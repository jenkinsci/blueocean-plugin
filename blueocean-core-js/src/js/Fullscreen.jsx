import React, { PropTypes } from 'react';

/**
 * Simple component to render a full-page overlay without any decoration
 */
export const Fullscreen = ({ className, children }) => <div className={`fullscreen ${className}`}>{children}</div>;

Fullscreen.propTypes = {
    className: PropTypes.string,
    children: PropTypes.any,
};
