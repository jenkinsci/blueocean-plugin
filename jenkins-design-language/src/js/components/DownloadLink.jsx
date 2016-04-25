import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

const { object } = PropTypes;

class DownloadLink extends Component {
    render() {
        const { style, fileData } = this.props;
        if (!fileData) return null;
        const {
          contents,
          mime,
          filename,
        } = fileData;
        const blob = new Blob([contents], { type: mime });
        const url = URL.createObjectURL(blob);

        return (<a {...{
            download: filename,
            href: url,
            title: 'Download the log',
        }}
        >
            <Icon {...{ style, icon: 'file_download' }} />
        </a>);
    }
}
DownloadLink.propTypes = {
    fileData: object,
    style: object,
};
export {
  DownloadLink,
};
