import React, { Component, PropTypes } from 'react';

const { number, string } = PropTypes;

class LogConsole extends Component {
    render() {
        const {result} = this.props;
        //early out
        if (!result) {
            return null;
        }
        return (<code
          dangerouslySetInnerHTML={{__html: result}}
          className="block"
        />)
    }
}

LogConsole.propTypes = {
    result: string.isRequired,
};

export {LogConsole};
