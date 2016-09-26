// @flow

import Extensions from '@jenkins-cd/js-extensions';

const ext = require('@jenkins-cd/js-extensions');
ext.store.init({
    extensionDataProvider: (cb) => {
        cb([]);
    },
    typeInfoProvider: (type, cb) => {
        cb(null);
    },
});


require('./editorStories');
