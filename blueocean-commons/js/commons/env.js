const env = require('targetenv');

env.debug = process.env.debug ? true : false;
if (env.isBrowser) {
  env.browser = {};//FIXME implement browser detection
}
if (env.isBrowser && window.appSettings) {
  const settings = JSON.parse(window.appSettings);
  env.NODE_ENV = (settings.NODE_ENV) ? settings.NODE_ENV : process.env.NODE_ENV;
} else {
  env.NODE_ENV = process.env.NODE_ENV;
}

// revision info is gathered during webpack build and set to "process.env" (display this in development footer)
env.revisionInfo = process.env.buildRevisionInfo;

export default env;
