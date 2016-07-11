import React from 'react';

/**
 * Simple component to render a full-page overlay without any decoration
 */
export default (props) => (
    <div className={`fullscreen ${props.className}`}>
        {props.children}
    </div>
);
