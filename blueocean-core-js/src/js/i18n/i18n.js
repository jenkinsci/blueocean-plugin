import i18next from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';

import urlConfig from '../urlconfig';

/**
 * Init language detector, we are going to use first queryString and then the navigator prefered language
 */
export const defaultLngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
});
const prefix = urlConfig.getJenkinsRootURL() || '/';

const loadPath = `${prefix}/i18n/resourceBundle?language={lng}&baseName={ns}`;
/**
 * configure the backend for our locale
 */
export const defaultXhr = new XHR(null, {
    loadPath,
    allowMultiLoading: false,
    parse: (data) => {
        // we need to parse the response and then extract the data since the rest is garbage for us
        const response = JSON.parse(data);
        return response.data;
    },
});

/**
 * Our default properties for i18next
 * @type {{fallbackLng: string, ns: string[], defaultNS: string, preload: string[], keySeparator: boolean, debug: boolean, load: string, interpolation: {prefix: string, suffix: string, escapeValue: boolean}}}
 */
export const initOptions = {
    fallbackLng: 'en',
    // have a common namespace used around the full app
    ns: ['jenkins.plugins.blueocean.web.Messages', 'jenkins.plugins.blueocean.dashboard.Messages'],
    defaultNS: 'jenkins.plugins.blueocean.web.Messages',
    preload: ['en'],
    keySeparator: false, // we do not have any nested keys in properties files
    debug: false,
    load: 'all', // --> ['en-US', 'en', 'dev']
    interpolation: {
        prefix: '{',
        suffix: '}',
        escapeValue: false, // not needed for react!!
    },
};

/**
 * Create a instance of i18next and init it
 * in case we are in test mode and run unit test, we deliver a i18next instance that are not using any backend nor language detection
 * @param backend  {object} - the backend we want to use
 * @param lngDetector {object} - the component that detects which language we want to display
 * @param options {object} - general options for i18next
 * @see defaultOptions
 */
export const i18n = (backend = defaultXhr, lngDetector = defaultLngDetector, options = initOptions) => {
    if (typeof window === 'undefined') {  // eslint-disable-line no-undef
        return i18next.init({
            lng: 'en',
            // have a common namespace used around the full app
            ns: ['translation'],
            defaultNS: 'translation',
            preload: ['en'],
            keySeparator: false, // we do not have any nested keys in properties files
            interpolation: {
                prefix: '{',
                suffix: '}',
                escapeValue: false, // not needed for react!!
            },
            resources: {
                en: {
                    translation: {
                        'home.pipelineslist.row.failing': '{0} failing',
                        'home.pipelineslist.row.passing': '{0} passing',
                    },
                },
            },
        });
    }
    return i18next
        .use(backend)
        .use(lngDetector)
        .init(options);
};


export default i18n(defaultXhr, defaultLngDetector, initOptions);
