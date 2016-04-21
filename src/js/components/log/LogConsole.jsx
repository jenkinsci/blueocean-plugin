import React, { Component, PropTypes } from 'react';
import {fetch} from '../fetch';
import {Icons} from 'react-material-icons-blue';
import {DownloadLink} from '../DownloadLink';

const {string} = PropTypes;
let rawUrl;

class LogConsole extends Component {
    render() {
        const {data, file} = this.props;
        //early out
        if (!data) {
            return null;
        }
        var fileData = {
            filename: rawUrl,
            contents: data,
            mime: 'text/plain',
        };

        let lines = [];
        if (data && data.split) {
            lines = data.split('\n');
        }

        return (<code
          className="block"
        >
            <div className="log-header download-log-button right">
                <DownloadLink fileData={fileData}/>

                <a
                  title="Display the log in new window"
                  target="_blank"
                  href={rawUrl}
                >
                    <Icons
                      icon="link"// Icons in the field transformation
                      style={{ fill: "#fff" }} // Styles prop for icon (svg)
                    />
                    Open
                </a>
            </div>
            {lines.map((line, index) => <p key={index}>
                <a key={index} name={index}>{line}</a>
            </p>)}</code>)
    }
}

LogConsole.propTypes = {
    data: string,
    file: string,
    url: string.isRequired,
};

export default fetch(LogConsole, ({url}, config) => {
    rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;
