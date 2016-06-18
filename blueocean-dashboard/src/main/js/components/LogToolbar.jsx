import React, { Component, PropTypes } from 'react';
import { DownloadLink } from '@jenkins-cd/design-language';

import { Icon } from 'react-material-icons-blue';

const { string } = PropTypes;


// FIXME: add fetchRunLog in actions for getting data
export default class LogToolbar extends Component {
    render() {
        const { fileName, data, url, title } = this.props;
        // early out
        if (!url) {
            return null;
        }
        const style = { fill: '#4a4a4a' };
        return (<div className="log-header">
            <div className="log-header__section">
                {title}
            </div>
            <div className="log-header__section download-log-button">
              { data && <DownloadLink {...{ style, fileData: {
                  filename: fileName,
                  contents: data,
                  mime: 'text/plain',
              } }}/>}

                <a {...{
                    title: 'Display the log in new window',
                    target: '_blank',
                    href: `${url}?start=0`,
                }}>
                    <Icon size={24} {...{ style, icon: 'launch' }} />
                </a>
            </div>
        </div>);
    }
}

LogToolbar.propTypes = {
    data: string,
    title: string,
    fileName: string,
    url: string.isRequired,
};
