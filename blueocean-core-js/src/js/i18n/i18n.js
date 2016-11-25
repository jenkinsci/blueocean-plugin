import i18next from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';
import { store } from '@jenkins-cd/js-extensions';

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
const prefix = urlConfig.getJenkinsRootURL() || '';

function newPluginXHR(pluginName) {
    let pluginVersion = store.getPluginVersion(pluginName);

    if (!pluginVersion) {
        throw new Error(`Unable to create an i18n instance for plugin "${pluginName}". This plugin is not currently installed, or is disabled.`);
    }

    pluginVersion = encodeURIComponent(pluginVersion);

    const loadPath = `${prefix}/blueocean-i18n/${pluginName}/${pluginVersion}/{ns}/{lng}`;
    return new XHR(null, {
        loadPath,
        allowMultiLoading: false,
        parse: (data) => {
            // we need to parse the response and then extract the data since the rest is garbage for us
            const response = JSON.parse(data);
            return response.data;
        },
    });
}

/**
 * Create a instance of i18next and init it
 * in case we are in test mode and run unit test, we deliver a i18next instance that are not using any backend nor language detection
 * @param backend  {object} - the backend XHR invoker we want to use
 * @param lngDetector {object} - the component that detects which language we want to display
 * @param options {object} - general options for i18next
 * @see defaultOptions
 */
export const i18n = (backend, lngDetector = defaultLngDetector, options) => {
    // TODO: Decide if we really want to export this i18n instance. Is there a use case for it?
    // If there's no use case for exporting it, then general rule is not to do that. "Good fences make for good neighbours" etc

    if (!backend) {
        throw new Error('Invalid call to create a new i18next instance. No backend XHR invoker supplied.');
    }
    if (!options) {
        throw new Error('Invalid call to create a new i18next instance. No i18next options supplied.');
    }
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
    return i18next.createInstance()
        .use(backend)
        .use(lngDetector)
        .init(options);
};

const translatorCache = {};

/**
 * Create an i18n Translator instance for accessing i18n resource bundles
 * in the named plugin namespace.
 * @param pluginName The name of the plugin.
 * @param namespace The resource bundle namespace.
 * @return An i18n Translator instance.
 */
export default function i18nTransFactory(pluginName, namespace) {
    const translatorCacheKey = `${pluginName}:${namespace}`;
    let translator = translatorCache[translatorCacheKey];

    if (translator) {
        return translator;
    }

    const initOptions = {
        fallbackLng: 'en',
        ns: [namespace],
        defaultNS: namespace,
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

    const I18n = i18n(newPluginXHR(pluginName), defaultLngDetector, initOptions);

    // Create and cache the translator instance.
    translator = I18n.getFixedT(I18n.language, namespace);
    translatorCache[translatorCacheKey] = translator;

    return translator;
}
