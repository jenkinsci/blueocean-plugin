// this function is executed before all other code during bundle's onStartup
// see gulpfile, builder.bundle().onStartup()

import ErrorUtils from './ErrorUtils';

export function execute(done, bundleConfig) {
    ErrorUtils.initializeErrorHandling();
    done();
}
