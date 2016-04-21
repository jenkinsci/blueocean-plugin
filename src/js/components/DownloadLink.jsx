import React, { Component, PropTypes } from 'react';
import {Icons} from 'react-material-icons-blue';

const {object} = PropTypes;

class DownloadLink extends Component {
    saveAs(uri, filename) {
        const link = document.createElement('a');
        if (typeof link.download === 'string') {
            document.body.appendChild(link); //Firefox requires the link to be in the body
            link.download = filename;
            link.href = uri;
            link.click();
            document.body.removeChild(link); //remove the link when done
        } else {
            location.replace(uri);
        }
    }


    render() {
        const onDownload = () => {
            const {fileData} = this.props;
            if(!fileData) return null;
            const {
              contents,
              mime,
              filename,
            } = fileData;
            var blob = new Blob([contents], {type: mime})
              , url = URL.createObjectURL(blob)
            this.saveAs(url, filename)
        }
         return  <a
             title="Download the log"
             onClick={onDownload}
           >
             <Icons
               icon="file_download"// Icons in the field transformation
               style={{ fill: "#fff" }} // Styles prop for icon (svg)
             />
             Download
           </a>
    }
}
DownloadLink.propTypes = {
    fileData: object,
};
export {
    DownloadLink,
}
