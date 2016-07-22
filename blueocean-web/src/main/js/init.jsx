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
    //
    // Application initialization.
    //
    const jenkinsMods = require('@jenkins-cd/js-modules');

    // Create and export the shared js-extensions instance. This
    // will be accessible to bundles from plugins etc at runtime, allowing them to register
    // extension point impls that can be rendered via the ExtensionPoint class.
    const Extensions = require('@jenkins-cd/js-extensions');
    jenkinsMods.export('jenkins-cd', 'js-extensions', Extensions);

    // Create and export a shared instance of the design
    // language React classes.
    const jdl = require('@jenkins-cd/design-language');
    jenkinsMods.export('jenkins-cd', 'jdl', jdl);

    // Load and export the react modules, allowing them to be imported by other bundles.
    const react = require('react');
    const reactDOM = require('react-dom');
    const reactCSSTransitions = require('react-addons-css-transition-group');
    jenkinsMods.export('react', 'react', react);
    jenkinsMods.export('react', 'react-dom', reactDOM);
    jenkinsMods.export('react', 'react-addons-css-transition-group', reactCSSTransitions);

    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    const appRoot = document.getElementsByTagName("head")[0].getAttribute("data-appurl");
    Extensions.init({
        extensionDataProvider: cb => getURL(`${appRoot}/js-extensions`, rsp => cb(rsp.data)),
        classMetadataProvider: (type, cb) => getURL(`${appRoot}/rest/classes/${type}/`, cb)
    });
    oncomplete();
};
