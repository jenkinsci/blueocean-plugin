import React, { Component, PropTypes } from 'react';

export default class RunIdCell extends Component {
    propTypes = {
        run: PropTypes.object,
    };

    render() {
        const identifier = this.props.run.name ? this.props.run.name : this.props.run.id;
        return (<span title={identifier}>{identifier}</span>);
    };
}
