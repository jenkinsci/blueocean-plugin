const ext = require('@jenkins-cd/js-extensions');
ext.store.init({
    extensionData: [],
    typeInfoProvider: (type, cb) => {
        cb(null);
    },
});

require('./pipelines');
require('./status');
require('./icons');
require('./RunDetailsHeaderStories');
require('./FullScreenStories');
require('./moments');
require('./inputStep');
