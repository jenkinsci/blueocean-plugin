import React, { Component, PropTypes } from 'react';

export default class RunCause extends Component {
    propTypes = {
        run: PropTypes.object,
    };

    render() {
        const run = this.props.run;
        const cause = (run && this.props.run.causes.length > 0 && this.props.run.causes[0].shortDescription) || null;
        return cause ? (<div className="causes">{cause}</div>) : null;
    }
}
