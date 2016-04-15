import React, { Component, PropTypes } from 'react';

const {string} = PropTypes;

class LogConsole extends Component {
    render() {
        const {result} = this.props;
        //early out
        if (!result) {
            return null;
        }

        let lines = [];
        if (result && result.split) {
            lines = result.split('\n');
        }

        return (<code
          className="block"
        >{lines.map((line, index) => <p key={index}>
            <a key={index} name={index}>{line}</a>
        </p>)}</code>)
    }
}

LogConsole.propTypes = {
    result: string.isRequired,
};

export {LogConsole};
