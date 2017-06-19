import React, { PropTypes } from 'react';
import { Link } from 'react-router';

// Fixme: Splat these post table-transistion

/**
 * A convenience wrapper to pass down 'linkUrl' to child CellLink components.
 */
export function CellRow(props) {
    return (
        <tr id={props.id}>
            {React.Children.map(
                props.children,
                child => React.cloneElement(child, { linkUrl: props.linkUrl })
            )}
        </tr>
    );
}

CellRow.propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
    linkUrl: PropTypes.string,
};

/**
 * A table cell with an embedded react-router link.
 */
export function CellLink(props) {
    const url = props.linkUrl || '';
    const extraClass = !props.disableDefaultPadding ? 'u-link-padding' : '';

    return (
        <td className={`cell-link ${extraClass}`}>
            <Link to={url}>{props.children}</Link>
        </td>
    );
}

CellLink.propTypes = {
    children: PropTypes.node,
    linkUrl: PropTypes.string,
    disableDefaultPadding: PropTypes.bool,
};
