import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { fetchAllSuffix as suffix } from '../../../util/UrlUtils';

import moment from 'moment';
require('moment-duration-format');
// needs to be loaded since the moment lib will use require which in run time will fail
import 'moment/min/locales.min';

const { string } = PropTypes;

export default class LogToolbar extends Component {
    render() {
        const { url, title, duration } = this.props;
        const displayFormat = 'd[d] h[h] m[m] s[s]';
        const computedTitle = duration ? title + ' - ' + (moment.duration(duration).format(displayFormat)) : title;
        // early out
        if (!url) {
            return null;
        }
        const logUrl = url.includes(suffix) ? url : `${url}${suffix}`;
        const style = { fill: '#4a4a4a' };

        return (<div className="log-header">
            <div className="log-header__section selected">
                {computedTitle}
            </div>
            <div className="log-header__section download-log-button">
                <a {...{
                    title: 'Display the log in new window',
                    target: '_blank',
                    href: logUrl,
                }}
                >
                    <Icon size={24} {...{ style, icon: 'launch' }} />
                </a>
                <a
                {...{
                    title: 'Download the log file',
                    href: `${logUrl}&download=true`,
                }}
                >
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
    duration: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
};
