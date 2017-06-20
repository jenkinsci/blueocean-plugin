import i18next from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import { store } from '@jenkins-cd/js-extensions';
import XHR from 'i18next-xhr-backend';

import urlConfig from '../urlconfig';
import logging from '../logging';
import { Fetch } from '../fetch';

const logger = logging.logger('io.jenkins.blueocean.i18n');

/**
 * Init language detector, we are going to use first queryString and then the navigator prefered language
 */
export const defaultLngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'htmlTag', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
    // Don't use the default (document.documentElement) because that can
    // trigger the browsers auto-translate, which is quite annoying.
    htmlTag: (window.document ? window.document.head : undefined),
});
const prefix = urlConfig.getJenkinsRootURL() || '';
const FALLBACK_LANG = '';

function newPluginXHR(pluginName, onLoad) {
    let pluginVersion = store.getPluginVersion(pluginName);

    if (!pluginVersion) {
        // if we do not have a version we may have an alias to resolve a resourceBUndle
        throw new Error(`Unable to create an i18n instance for plugin "${pluginName}". This plugin is not currently installed, or is disabled.`);
    }

    pluginVersion = encodeURIComponent(pluginVersion);

    const loadPath = `${prefix}/blue/rest/i18n/${pluginName}/${pluginVersion}/{ns}/{lng}`;
    return new XHR(null, {
        loadPath,
        allowMultiLoading: false,
        ajax: (url, options, callback) => {
            if (logger.isDebugEnabled()) {
                logger.debug('loading data for', url);
            }
            let status;
            // eslint-disable-next-line
            return Fetch.fetch(url, { disableLoadingIndicator: true, ignoreRefreshHeader: true })
                .then(response => {
                    // i18n xhr-backend needs the status
                    status = response.status;
                    // now return the raw data
                    return response.text();
                })
                .then((data) => {
                    if (callback) {
                        const xhr = { status };
                        if (logger.isDebugEnabled()) {
                            logger.debug('calling now callback with xhr and data', xhr, data);
                        }
                        callback(data, xhr);
                    }
                });
        },
        parse: (data) => {
            // we need to parse the response and then extract the data since the rest is garbage for us
            const response = JSON.parse(data);
            if (logger.isDebugEnabled()) {
                logger.debug('Received i18n resource bundle for plugin "%s".', pluginName, response.data);
            }
            if (typeof onLoad === 'function') {
                onLoad();
            }
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
let useMockFallback = false;

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
const pluginI18next = (pluginName, namespace = toDefaultNamespace(pluginName), onLoad = undefined) => {
    assertPluginNameDefined(pluginName);

    const initOptions = {
        ns: [namespace],
        defaultNS: namespace,
        keySeparator: false, // we do not have any nested keys in properties files
        debug: false,
        fallbackLng: FALLBACK_LANG,
        load: 'currentOnly',
        interpolation: {
            prefix: '{',
            suffix: '}',
            escapeValue: false, // not needed for react!!
        },
    };

    return i18nextInstance(newPluginXHR(pluginName, onLoad), defaultLngDetector, initOptions);
};

function buildCacheKey(pluginName, namespace = toDefaultNamespace(pluginName)) {
    return `${pluginName}:${namespace}`;
}

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
export default function i18nTranslator(pluginName, namespace, onLoad) {
    assertPluginNameDefined(pluginName);

    const translatorCacheKey = buildCacheKey(pluginName, namespace);
    let translator = translatorCache[translatorCacheKey];

    if (translator) {
        if (typeof onLoad === 'function') {
            onLoad();
        }
        return translator;
    }

    // Lazily construct what we need instead of on creation
    return function translate(key, params) {
        if (useMockFallback) {
            return (params && params.defaultValue) || key;
        }

        if (!translator) {
            const I18n = pluginI18next(pluginName, namespace, onLoad);

            // Create and cache the translator instance.
            let detectedLang;
            try {
                detectedLang = defaultLngDetector.detect();
            } catch (e) {
                detectedLang = FALLBACK_LANG;
            }

            if (logger.isLogEnabled()) {
                logger.log('Translator instance created for "%s". Language detected as "%s".', translatorCacheKey, detectedLang);
            }

            const fixedT = I18n.getFixedT(detectedLang, namespace);
            translator = function (i18nKey, i18nParams) {
                const normalizedKey = i18nKey.replace(/[\W]/g, '.');
                let passedParams = i18nParams;
                if (normalizedKey !== i18nKey) {
                    if (!passedParams) {
                        passedParams = {};
                    }
                    if (!passedParams.defaultValue) {
                        passedParams.defaultValue = i18nKey;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(`Normalized i18n key "${i18nKey}" to "${normalizedKey}".`);
                    }
                }
                return fixedT(normalizedKey, passedParams);
            };
            translatorCache[translatorCacheKey] = translator;
        }

        if (key) {
            return translator(key, params);
        }
        return undefined;
    };
}

export function enableMocksForI18n() {
    useMockFallback = true;
}

export function disableMocksForI18n() {
    useMockFallback = false;
}

