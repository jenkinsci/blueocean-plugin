//
// TODO: Auto-gen this from jenkins-js-extension.yaml
//
// The shared extension point store is (will be) loaded by blueocean-web.
// Import that shared instance and use it to register the extension points
// listed in jenkins-js-extension.yaml.
//

import AdminNavLink from './AdminNavLink.jsx';
require('jenkins-js-modules').import('jenkins-cd:js-extensions')
    .onFulfilled(function(extensions) {
        // Manually register extention points. TODO: we will be auto-registering these.
        extensions.store.addExtension("jenkins.topNavigation.menu", AdminNavLink);
    });
