import React, { Component, PropTypes } from 'react';
import { fetch } from '../fetch';

const { string } = PropTypes;

class LogConsole extends Component {
    render() {
        const { data } = this.props;

        let lines = [];
        if (data && data.split) {
            lines = data.split('\n');
        }

        return (<code
          className="block"
        >
            {lines.map((line, index) => <p key={index}>
                <a key={index} name={index}>{line}</a>
            </p>)}</code>);
    }
}

LogConsole.propTypes = {
    data: string,
    file: string,
    url: string.isRequired,
};

export default fetch(LogConsole, ({ url }, config) => {
    const rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;
