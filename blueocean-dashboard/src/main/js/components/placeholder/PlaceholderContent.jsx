import React, { PropTypes } from 'react';

/**
 * Simple wrapper that holds a PlaceholderDialog and positions it.
 * @param props
 * @returns {React.Element}
 * @constructor
 */
export function PlaceholderContent(props) {
    const { children, className, style } = props;
    const classString = `PlaceholderContent ${className}`;

    return (
        <main className={classString} style={style}>
            <article>{children}</article>
        </main>
    );
}

PlaceholderContent.propTypes = {
    children: PropTypes.element,
    className: PropTypes.element,
    style: PropTypes.style,
};

PlaceholderContent.defaultProps = {
    className: '',
};
