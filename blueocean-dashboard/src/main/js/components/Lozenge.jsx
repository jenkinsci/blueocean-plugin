import React, { Component, PropTypes } from 'react';

export default class Lozenge extends Component {
    propTypes = {
        title: PropTypes.string,
    };

    render() {
        const title = this.props.title;
        return (<span className="Lozenge" title={title}>{title}</span>);
    }
}
