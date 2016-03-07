import AboutNavLink from './about/AboutNavLink.jsx';

exports.initialize = function (oncomplete) {
    //
    // Application initialization.
    //
    const jenkinsMods = require('@jenkins-cd/js-modules');

    // Create and export the shared js-extensions instance. This
    // will be accessible to bundles from plugins etc at runtime, allowing them to register
    // extension point impls that can be rendered via the ExtensionPoint class.
    const extensions = require('@jenkins-cd/js-extensions');
    jenkinsMods.export('jenkins-cd', 'js-extensions', extensions);

    // Create and export a shared instance of the design
    // language React classes.
    var jdl = require('@jenkins-cd/design-language');
    jenkinsMods.export('jenkins-cd', 'jdl', jdl);

    // Load and export the react modules, allowing them to be imported by other bundles.
    const react = require('react');
    const reactDOM = require('react-dom');
    jenkinsMods.export('react', 'react', react);
    jenkinsMods.export('react', 'react-dom', reactDOM);

    // Manually register extention points. TODO: we will be auto-registering these.
    extensions.store.addExtension('jenkins.topNavigation.menu', AboutNavLink);

    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    const $ = require('jquery-detached').getJQuery();
    const jenkinsRoot = jenkinsMods.getRootURL();
    $.getJSON(`${jenkinsRoot}/blue/javaScriptExtensionInfo`, (data) => {
        extensions.store.setExtensionPointMetadata(data);
        oncomplete();
    });
};
