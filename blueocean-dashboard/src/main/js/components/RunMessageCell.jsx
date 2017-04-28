import React, { Component, PropTypes } from 'react';

export default class RunMessageCell extends Component {
    propTypes = {
        run: PropTypes.object,
    };

    render() {
        const run = this.props.run;
        let message;
        if (run && run.description) {
            message = run.description;
        } else if (run && run.changeSet && run.changeSet.length > 0) {
            message = run.changeSet[run.changeSet.length - 1].msg;
        } else if (run && run.causes.length > 0) {
            message = run.causes[0].shortDescription;
        } else {
            message = '–';
        }
        return (<span className="message" title={message}>{message}</span>);
    }
}
