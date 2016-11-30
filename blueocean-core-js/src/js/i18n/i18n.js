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
const i18nextInstance = (backend, lngDetector = defaultLngDetector, options) => {
    if (!backend) {
        throw new Error('Invalid call to create a new i18next instance. No backend XHR invoker supplied.');
    }
    if (!options) {
        throw new Error('Invalid call to create a new i18next instance. No i18next options supplied.');
    }
    return i18next.createInstance()
        .use(backend)
        .use(lngDetector)
        .init(options);
};

const translatorCache = {};

const assertPluginNameDefined = (pluginName) => {
    if (!pluginName) {
        throw new Error('"pluginName" arg cannot be null/blank');
    }
};

const toDefaultNamespace = (pluginName) => {
    assertPluginNameDefined(pluginName);
    // Replace all hyphen chars with a dot.
    return `jenkins.plugins.${pluginName.replace(/-/g, '.')}.Messages`;
};

/**
 * Create an i18next instance for accessing i18n resource bundles
 * in the named plugin namespace.
 * @param pluginName The name of the plugin.
 * @param namespace The resource bundle namespace. Optional, defaulting to
 * the plugin's default resource bundle e.g. "jenkins.plugins.blueocean.web.Messages"
 * for the "blueocean-web" plugin and "jenkins.plugins.blueocean.dashboard.Messages"
 * for the "blueocean-dashboard" plugin.
 * @return An i18n instance.
 */
const pluginI18next = (pluginName, namespace = toDefaultNamespace(pluginName)) => {
    assertPluginNameDefined(pluginName);

    const initOptions = {
        ns: [namespace],
        defaultNS: namespace,
        keySeparator: false, // we do not have any nested keys in properties files
        debug: false,
        fallbackLng: '',
        load: 'currentOnly',
        interpolation: {
            prefix: '{',
            suffix: '}',
            escapeValue: false, // not needed for react!!
        },
    };

    return i18nextInstance(newPluginXHR(pluginName), defaultLngDetector, initOptions);
};

/**
 * Create an i18n Translator instance for accessing i18n resource bundles
 * in the named plugin namespace.
 * @param pluginName The name of the plugin.
 * @param namespace The resource bundle namespace. Optional, defaulting to
 * the plugin's default resource bundle e.g. "jenkins.plugins.blueocean.web.Messages"
 * for the "blueocean-web" plugin and "jenkins.plugins.blueocean.dashboard.Messages"
 * for the "blueocean-dashboard" plugin.
 * @return An i18n Translator instance.
 */
export default function i18nTranslator(pluginName, namespace = toDefaultNamespace(pluginName)) {
    assertPluginNameDefined(pluginName);

    const translatorCacheKey = `${pluginName}:${namespace}`;
    let translator = translatorCache[translatorCacheKey];

    if (translator) {
        return translator;
    }

    const I18n = pluginI18next(pluginName, namespace);

    // Create and cache the translator instance.
    translator = I18n.getFixedT(defaultLngDetector.detect(), namespace);
    translatorCache[translatorCacheKey] = translator;

    return translator;
}
