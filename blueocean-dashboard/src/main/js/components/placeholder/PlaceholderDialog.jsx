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
    // use 'linkElement' as is, or build a link if the text and href were supplied
    const link =
        linkElement ||
        (linkText && linkHref && (
            <a className="btn" target="_blank" href={linkHref}>
                {linkText}
            </a>
        ));

    return [icon && React.cloneElement(icon, { className: 'icon' }), <h1 className="title">{title}</h1>, message && <p className="message">{message}</p>, link];
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
