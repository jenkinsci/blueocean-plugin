const ext = require('@jenkins-cd/js-extensions');
ext.store.init({
    extensionDataProvider: (cb) => {
        cb([]);
    },
    typeInfoProvider: (type, cb) => {
        cb(null);
    },
});

require('./pipelines');
require('./status');
require('./icons');
require('./logDisplay');
