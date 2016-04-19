import React, { Component, PropTypes } from 'react';
import {fetch} from '../fetch';

const {string} = PropTypes;
let rawUrl;

class LogConsole extends Component {
    render() {
         const {data} = this.props;
         //early out
         if (!data) {
            return null;
         }

         let lines = [];
         if (data && data.split) {
            lines = data.split('\n');
         }

        return (<code
              className="block"
            >
                <div className="log-header">
                    <a
                      className="download-log-button right"
                      title="Display the log in plaintext"
                      target="_blank"
                      href={rawUrl}
                    >
                        <span className="icon-download-log" aria-hiden="true"></span>Raw log</a>
                </div>
                {lines.map((line, index) => <p key={index}>
                <a key={index} name={index}>{line}</a>
            </p>)}</code>)
    }
}

LogConsole.propTypes = {
    data: string,
    url: string.isRequired,
};

export default fetch(LogConsole, ({url}, config) => {
    rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;
