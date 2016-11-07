import i18next from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';
import EventEmitter from 'events';

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
    ns: ['jenkins.plugins.blueocean.web.Messages'],
    defaultNS: 'jenkins.plugins.blueocean.web.Messages',
    preload: ['en'],
    keySeparator: false, // we do not have any nested keys in properties files
    debug: true, // for dev reasons ATM
    load: 'all', // --> ['en-US', 'en', 'dev']
    interpolation: {
        prefix: '{',
        suffix: '}',
        escapeValue: false, // not needed for react!!
    },
};

/**
 * Create a instance of i18next and init it
 * @param backend  {object} - the backend we want to use
 * @param lngDetector {object} - the component that detects which language we want to display
 * @param options {object} - general options for i18next
 * @see defaultOptions
 */
export const defaultI18n = (backend = defaultXhr, lngDetector = defaultLngDetector, options = initOptions) => i18next
  .use(backend)
  .use(lngDetector)
  .init(options);

export class I18nApi extends EventEmitter {
    /**
     * Default constructor, if invoked without arguments we will fallback to default values
     * @param namespaces - the namespaces we want to load
     * @param options - common flags to change the default behaviour
     */
    constructor(namespaces = initOptions.ns, options = {}) {
        super();
        const {
            wait = true,
            bindI18n = 'languageChanged loaded',
            bindStore = 'added removed',
            i18n = defaultI18n(),
        } = options;
        this.locale = i18n.language;
        /**
         * As translate function we pin down the namespaces and the language we want to use
         */
        this.translate = i18n.getFixedT(this.locale, namespaces);
        /**
         * when we want to remove the component we need to clean up
         */
        this.unmount = () => {
            if (this.onI18nChanged) {
                bindI18n.split(' ').forEach((event) => {
                    i18n.off(event, this.onI18nChanged);
                });
                bindStore.split(' ').forEach((event) => {
                    i18n.store.off(event, this.onI18nChanged);
                });
            }
            this.mounted = false;
        };
        /**
         * when someone changes the language we emit a 'i18nChanged' event
         */
        this.onI18nChanged = () => {
            if (!this.mounted) return;
            this.emit('i18nChanged', new Date());
        };
        /**
         * Standard implementation of the event (just log it)
         */
        this.on('i18nChanged', (date) => {
            console.log('i18nChanged', date, 'language', this.locale);
        });

        const bindToI18n = () => {
            if (bindI18n) {
                i18n.on(bindI18n, this.onI18nChanged);
            }
            if (bindStore && i18n.store) {
                i18n.store.on(bindStore, this.onI18nChanged);
            }
        };

        /**
         * We can wait that all ns are loaded and then fire our events.
         */
        i18n.loadNamespaces(namespaces, () => {
            this.mounted = true;
            if (wait) bindToI18n();
        });

        if (!wait) bindToI18n();
    }
}
