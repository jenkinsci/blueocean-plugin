import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

export default class Lozenge extends Component {
    propTypes = {
        title: PropTypes.string,
        linkTo: PropTypes.string,
    };

    render() {
        const { linkTo, title } = this.props;

        if (linkTo) {
            return (
                <Link to={linkTo} className="Lozenge" title={title}>{title}</Link>
            );
        }

        return (
            <span className="Lozenge" title={title}>{title}</span>
        );
    }
}
