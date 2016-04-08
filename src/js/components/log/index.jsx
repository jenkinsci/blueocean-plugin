import React, { Component, PropTypes } from 'react';

const {any} = PropTypes;

class LogConsole extends Component {
    render() {
        const {result} = this.props;
        //early out
        if (!result) {
            return null;
        }
        return (<code
          className="block"
        >{result}</code>)
    }
}

LogConsole.propTypes = {
    result: any.isRequired,
};

export {LogConsole};
