const ext = require('@jenkins-cd/js-extensions');
ext.store.init({
    extensionData: [],
    typeInfoProvider: (type, cb) => {
        cb(null);
    },
});

require('./ColumnFilterStories');
require('./FullScreenStories');
require('./icons');
require('./inputStep');
require('./moments');
require('./pipelines');
require('./RunDetailsHeaderStories');
require('./status');
