import React, { PropTypes } from 'react';


/**
 * Simple wrapper that holds a PlaceholderDialog and positions it.
 * @param props
 * @returns {React.Element}
 * @constructor
 */
export function PlaceholderContent(props) {
    return (
        <main className="PlaceholderContent">
            <article>
                {props.children}
            </article>
        </main>
    );
}

PlaceholderContent.propTypes = {
    children: PropTypes.element,
};
