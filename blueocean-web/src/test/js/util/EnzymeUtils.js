import { store as ExtensionStore } from '@jenkins-cd/js-extensions';
const jsdom = require('jsdom').jsdom;

/**
 * Prepares Enzyme's "mount" function for use by binding it to JSDOM.
 * Also takes care of a little bootstrapping of @js-extensions/ExtensionStore to avoid errors.
 * Place this snippet at the very top of your test file, before importing React: *
 * import { prepareMount } from './EnzymeUtils';
 * prepareMount();
 *
 * Created by cmeyers on 7/12/16.
 */
export const prepareMount = () => {
    // code to boostrap mount with JSDOM
    // see: https://github.com/airbnb/enzyme/blob/master/docs/guides/jsdom.md
    const exposedProperties = ['window', 'navigator', 'document'];
    global.document = jsdom('');
    global.window = document.defaultView;
    Object.keys(document.defaultView).forEach((property) => {
        if (typeof global[property] === 'undefined') {
            exposedProperties.push(property);
            global[property] = document.defaultView[property];
        }
    });

    global.navigator = {
        userAgent: 'node.js',
    };
};
