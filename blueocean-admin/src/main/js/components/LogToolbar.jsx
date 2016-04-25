import React, { Component, PropTypes } from 'react';
import { DownloadLink, fetch } from '@jenkins-cd/design-language';

import { Icon } from 'react-material-icons-blue';

const { string } = PropTypes;

let rawUrl;

class LogToolbar extends Component {
    render() {
        const { data, fileName } = this.props;
        // early out
        if (!data) {
            return null;
        }
        const style = { fill: '#4a4a4a' };
        return (<div className="log-header">
            <div className="log-header__section">
                Build log – Build > Build source
            </div>
            <div className="log-header__section download-log-button">
                <DownloadLink {...{ style, fileData: {
                    filename: fileName,
                    contents: data,
                    mime: 'text/plain',
                } }}
                />

                <a {...{
                    title: 'Display the log in new window',
                    target: '_blank',
                    href: rawUrl,
                }}
                >
                    <Icon {...{ style, icon: 'launch' }} />
                </a>
            </div>
        </div>);
    }
}

LogToolbar.propTypes = {
    data: string,
    fileName: string,
    url: string.isRequired,
};

export default fetch(LogToolbar, ({ url }, config) => {
    rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;

