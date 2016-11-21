import React, { PropTypes } from 'react';
import { Link } from 'react-router';

/**
 * A convenience wrapper to pass down 'linkUrl' to child CellLink components.
 */
export class CellRow extends React.Component {

    render() {
        return (
            <tr>
                {React.Children.map(
                    this.props.children,
                    child => React.cloneElement(child, { linkUrl: this.props.linkUrl })
                )}
            </tr>
        );
    }
}

CellRow.propTypes = {
    children: PropTypes.node,
    linkUrl: PropTypes.string,
};

/**
 * A table cell with an embedded react-router link.
 */
export function CellLink(props) {
    const url = props.linkUrl || '';

    return (
        <td className="tableRowLink">
            <Link to={url}>{props.children}</Link>
        </td>
    );
}

CellLink.propTypes = {
    children: PropTypes.node,
    linkUrl: PropTypes.string,
};
