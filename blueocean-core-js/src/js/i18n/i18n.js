import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';
import EventEmitter from 'events';

import urlConfig from '../UrlConfig';

/**
 * Init language detector, we are going to use first queryString and then the navigator prefered language
 */
export const defaultLngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
});
/**
 * configure the backend for our locale
 */
export const defaultXhr = new XHR(null, {
    loadPath: `${urlConfig.getJenkinsRootUrl()}/i18n/resourceBundle?language={lng}&baseName={ns}`,
    allowMultiLoading: false,
    parse: (data) => {
        // we need to parse the response and then extract the data since the rest is garbage for us
        const response = JSON.parse(data);
        return response.data;
    },
});

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

export const defaultI18n = (xhr = defaultXhr, lngDetector = defaultLngDetector, options = initOptions ) => i18n
  .use(xhr)
  .use(lngDetector)
  .init(options);

export class I18nApi extends EventEmitter {

    constructor(namespaces = initOptions.ns, options = {}) {
        super();
        const {
            wait = true,
            bindI18n = 'languageChanged loaded',
            bindStore = 'added removed',
            i18n = defaultI18n
        } = options;
        this.translate = i18n.getFixedT(i18n.language, namespaces);
        this.locale = i18n.language;
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

        this.onI18nChanged = () => {
            if (!this.mounted) return;
            this.emit('i18nChanged',  new Date());
        };

        this.on('i18nChanged', (date) => {
           console.log('i18nChanged', date, 'language', this.locale);
        });

        const bindToI18n = () => {
            bindI18n && i18n.on(bindI18n, this.onI18nChanged);
            bindStore && i18n.store && i18n.store.on(bindStore, this.onI18nChanged);
        };

        i18n.loadNamespaces(namespaces, () => {
            this.mounted = true;
            if (wait) bindToI18n();
        });

        if (!wait) bindToI18n();
    }
}

export const I18n = new I18nApi();
