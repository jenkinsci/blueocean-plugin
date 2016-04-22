import React, { Component, PropTypes } from 'react';
import { Icons } from 'react-material-icons-blue';

const { object } = PropTypes;

class DownloadLink extends Component {
    saveAs(uri, filename) {
        const link = document.createElement('a');
        if (typeof link.download === 'string') {
            document.body.appendChild(link); // Firefox requires the link to be in the body
            link.download = filename;
            link.href = uri;
            link.click();
            document.body.removeChild(link); // remove the link when done
        } else {
            location.replace(uri);
        }
    }

    render() {
        const onDownload = () => {
            const { fileData } = this.props;
            if (!fileData) return null;
            const {
              contents,
              mime,
              filename,
              } = fileData;
            const blob = new Blob([contents], { type: mime });
            const url = URL.createObjectURL(blob);
            return this.saveAs(url, filename);
        };
        const { style } = this.props;
        return (<a {...{
            onClick: onDownload,
            title: 'Download the log',
        }}
        >
            <Icons {...{ style, icon: 'file_download' }} />
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
