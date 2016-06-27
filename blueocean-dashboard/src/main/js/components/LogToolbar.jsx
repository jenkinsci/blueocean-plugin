import React, { Component, PropTypes } from 'react';

import { Icon } from 'react-material-icons-blue';

const { string } = PropTypes;


export default class LogToolbar extends Component {
    render() {
        const { url, title } = this.props;
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
                <a {...{
                    title: 'Display the log in new window',
                    target: '_blank',
                    href: `${url}?start=0`,
                }}>
                    <Icon size={24} {...{ style, icon: 'launch' }} />
                </a>
                <a {...{
                    title: 'Download the log file',
                    href: `${url}?start=0&download=true`,
                }}>
                    <Icon size={24} {...{ style, icon: 'file_download' }} />
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
