import React, { Component, PropTypes } from 'react';
import { Icon, TimeDuration } from '@jenkins-cd/design-language';
import { UrlUtils } from '@jenkins-cd/blueocean-core-js';

const { string } = PropTypes;

export default class LogToolbar extends Component {
    render() {
        const { url, title, duration, t, running, stageRestartLink } = this.props;

        // early out
        if (!url) {
            return null;
        }
        const logUrl = url.includes(UrlUtils.fetchAllSuffix) ? url : `${url}${UrlUtils.fetchAllSuffix}`;

        return (
            <div className="log-header">
                <div className="log-header__section selected">
                    <span>{title}</span>
                    {duration && (
                        <span>
                            <span>&nbsp;-&nbsp;</span>
                            <TimeDuration millis={duration} liveUpdate={running} updatePeriod={1000} t={t} />
                        </span>
                    )}
                </div>
                <div className="log-header__section download-log-button">
                    {stageRestartLink}
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
    duration: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    t: PropTypes.func,
    running: PropTypes.bool,
    stageRestartLink: PropTypes.element,
};
