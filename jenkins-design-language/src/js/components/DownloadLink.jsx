// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from './Icon';

const { object, string } = PropTypes;

class DownloadLink extends Component {
    render() {
        const { style, fileData, title = 'Download the log' } = this.props;
        if (!fileData) return null;
        const { contents, mime, filename } = fileData;
        const blob = new Blob([contents], { type: mime });
        const url = URL.createObjectURL(blob);

        return (
            <a
                {...{
                    download: filename,
                    href: url,
                    title: title,
                }}
            >
                <Icon {...{ style, icon: 'file_download' }} />
            </a>
        );
    }
}
DownloadLink.propTypes = {
    fileData: object,
    style: object,
    title: string,
};
export { DownloadLink };
