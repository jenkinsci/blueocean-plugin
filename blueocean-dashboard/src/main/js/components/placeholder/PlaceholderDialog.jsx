import React, { PropTypes } from 'react';

/**
 * Convert the dialog's "content" prop to appropriate elements for rendering.
 * @param content
 * @returns {Array<React.Element>}
 */
function createContent(content) {
    if (!content) {
        return null;
    }

    const { icon, title, message, linkElement, linkText, linkHref } = content;
    let newChildren = [];

    if (icon) {
        newChildren.push(React.cloneElement(icon, { className: 'icon', key: '1' }));
    }

    newChildren.push(
        <h1 className="title" key="2">
            {title}
        </h1>
    );

    if (message) {
        newChildren.push(
            <p className="message" key="3">
                {message}
            </p>
        );
    }

    // use 'linkElement' as is, or build a link if the text and href were supplied
    if (linkElement) {
        newChildren.push(linkElement);
    } else if (linkText && linkHref) {
        newChildren.push(
            <a className="btn" target="_blank" href={linkHref} key="4">
                {linkText}
            </a>
        );
    }

    return newChildren;
}

/**
 * Dialog that displays simple messaging for "no data" states.
 * Displays standard elements via 'content' prop, or custom content via 'children' prop.
 *
 * @param props
 * @returns {React.Element}
 * @constructor
 */
export function PlaceholderDialog(props) {
    const { children, style, width, content } = props;
    const newChildren = children || createContent(content);

    return (
        <div className="PlaceholderDialog" style={{ ...style, width }}>
            {newChildren}
        </div>
    );
}

PlaceholderDialog.propTypes = {
    children: PropTypes.element,
    style: PropTypes.object,
    width: PropTypes.number,
    content: PropTypes.shape({
        icon: PropTypes.node,
        title: PropTypes.string,
        message: PropTypes.string,
        linkText: PropTypes.string,
        linkHref: PropTypes.string,
        linkElement: PropTypes.node,
    }),
};

PlaceholderDialog.defaultProps = {
    width: 375,
};
