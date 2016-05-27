import React, { Component, PropTypes } from 'react';

const { string } = PropTypes;

export default class LogConsole extends Component {
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
};
