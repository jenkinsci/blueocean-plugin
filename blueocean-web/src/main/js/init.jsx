const requestDone = 4; // Because Zombie is garbage

// Basically copied from AjaxHoc
function getURL(url, onLoad) {
    const xmlhttp = new XMLHttpRequest();

    if (!url) {
        onLoad(null);
        return;
    }

    xmlhttp.onreadystatechange = () => {
        if (xmlhttp.readyState === requestDone) {
            if (xmlhttp.status === 200) {
                let data = null;
                try {
                    data = JSON.parse(xmlhttp.responseText);
                } catch (e) {
                    // eslint-disable-next-line
                    console.log('Loading', url,
                    'Expecting JSON, instead got', xmlhttp.responseText);
                }
                onLoad(data);
            } else {
                // eslint-disable-next-line
                console.log('Loading', url, 'expected 200, got', xmlhttp.status, xmlhttp.responseText);
            }
        }
    };
    xmlhttp.open('GET', url, true);
    xmlhttp.send();
}

exports.initialize = function (oncomplete) {
    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    const appRoot = document.getElementsByTagName("head")[0].getAttribute("data-appurl");
    const Extensions = require('@jenkins-cd/js-extensions');
    Extensions.init({
        extensionDataProvider: cb => getURL(`${appRoot}/js-extensions`, rsp => cb(rsp.data)),
        classMetadataProvider: (type, cb) => getURL(`${appRoot}/rest/classes/${type}/`, cb)
    });
    oncomplete();
};
