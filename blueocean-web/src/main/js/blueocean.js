// Initialise the Blue Ocean app.
require('./init.jsx');

var jenkinsMods = require('jenkins-js-modules');

//
// TODO: automate extensions bundle script loading from inside ExtensionPoint (on demand).
//
jenkinsMods.addScript('io/jenkins/blueocean-admin/jenkins-js-extensions.js', {
    scriptSrcBase: '@adjunct',
    success: function() {
        // start the App
        require('./main.jsx');
    }
});
