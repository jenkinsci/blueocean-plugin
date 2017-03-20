function getConfigAttribute(name) {
    var headElements = document.getElementsByTagName('head');
    if (headElements.length === 1) {
        return headElements[0].getAttribute(name);
    }
    return undefined;
}

function normalizeURL(location) {
    var normalizedUrl = 'http://anon.blueocean.io' + location;
    var rootUrl = getConfigAttribute('data-rooturl');
    if (rootUrl && location.startsWith(rootUrl)) {
        normalizedUrl = 'http://anon.blueocean.io' + location.substring(rootUrl.length - 1, location.length);
    }
    return normalizedUrl;
}

var transformer = function (payload) {
    payload.data.request.user_ip = '0.0.0.0';
    payload.data.request.url = normalizeURL(window.location.pathname);
};

//
// Configure rollbar ...
// See https://github.com/rollbar/rollbar.js/tree/master/examples/browserify
//

var _rollbarConfig = {
    accessToken: '81f3134dedf44871b9cc0a347b1313df',
    captureUncaught: true,
    code_version: window.$blueocean.config.version, // see header.jelly
    source_map_enabled: true,
    guess_uncaught_frames: true,
    transform: transformer
};

var rollbarBrowser = require('rollbar-browser');
var Rollbar = rollbarBrowser.init(_rollbarConfig);

// Looking at docs (https://github.com/rollbar/rollbar.js/tree/master/examples/browserify)
// it seems like they stuff it into a global. We are trying hard not to do that
// under any circumstances, but maybe this is an exception if it's only going to be
// used in a closed/controlled env.
//
// Soooo .... lets export it to global for now, but as $blueocean_Rollbar ...
//

window.$blueocean_Rollbar = Rollbar;

//
// Usage ...
//
//try {
//    foo();
//    $blueocean_Rollbar.debug('foo() called');
//} catch (e) {
//    $blueocean_Rollbar.error('Problem calling foo()', e);
//}

