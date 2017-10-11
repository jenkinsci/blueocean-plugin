import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { fetchAllSuffix as suffix } from '../../../util/UrlUtils';

const { string } = PropTypes;

export default class LogToolbar extends Component {
    render() {
        const { url, title } = this.props;
        // early out
        if (!url) {
            return null;
        }
        const logUrl = url.includes(suffix) ? url : `${url}${suffix}`;

        return (
            <div className="log-header">
                <div className="log-header__section selected">{title}</div>
                <div className="log-header__section download-log-button">
                    <a
                        {...{
                            title: 'Display the log in new window',
                            target: '_blank',
                            href: logUrl,
                        }}
                    >
                        <Icon icon="ActionLaunch" size={24} />
                    </a>
                    <a
                        {...{
                            title: 'Download the log file',
                            href: `${logUrl}&download=true`,
                        }}
                    >
                        <Icon icon="FileFileDownload" size={24} />
                    </a>
                </div>
            </div>
        );
    }
}

LogToolbar.propTypes = {
    data: string,
    title: string,
    fileName: string,
    url: string.isRequired,
};
