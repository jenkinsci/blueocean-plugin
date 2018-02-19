// @flow

import React, { Component, PropTypes } from 'react';

export class Alerts extends Component {
    render() {
        const { type = 'Info', message=`${type} message`, title=type } = this.props;
        const classNames = `Alert ${type}`;
        return (<div className={classNames}>
            <div className="border">&nbsp;</div>
            <div className="title-div">
                <span className="title">{title}</span>
                <span className="message">{message}</span>
            </div>

        </div>);
    }

    static propTypes = {
        type: PropTypes.oneOf(['Warning', 'Error', 'Success', 'Info']),
        message: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
        title: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
    };
}
