import AboutNavLink from './about/AboutNavLink.jsx';

exports.initialize = function(oncomplete) {
    //
    // Application initialization.
    //
    var jenkinsMods = require('@jenkins-cd/js-modules');

    // Create and export the shared js-extensions instance. This
    // will be accessible to bundles from plugins etc at runtime, allowing them to register
    // extension point impls that can be rendered via the ExtensionPoint class.
    var extensions = require('@jenkins-cd/js-extensions');
    jenkinsMods.export('jenkins-cd', 'js-extensions', extensions);

    // Load and export the react modules, allowing them to be imported by other bundles.
    var react = require('react');
    var reactDOM = require('react-dom');
    jenkinsMods.export('react', 'react', react);
    jenkinsMods.export('react', 'react-dom', reactDOM);

    // Manually register extention points. TODO: we will be auto-registering these.
    extensions.store.addExtension("jenkins.topNavigation.menu", AboutNavLink);

    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    var $ = require('jquery-detached').getJQuery();
    var jenkinsRoot = jenkinsMods.getRootURL();
    $.getJSON(jenkinsRoot + '/blue/javaScriptExtensionInfo', function(data) {
        extensions.store.setExtensionPointMetadata(data);
        oncomplete();
    });
};
