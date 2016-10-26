import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';
import urlConfig from '../UrlConfig';

/**
 * Init language detector, we are going to use first queryString and then the navigator prefered language
 */
export const lngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
});
/**
 * configure the backend for our locale
 */
export const xhr = new XHR(null, {
    loadPath: `${urlConfig.getJenkinsRootUrl()}/i18n/resourceBundle?language={lng}&baseName={ns}`,
    allowMultiLoading: false,
    parse: (data) => {
        // we need to parse the response and then extract the data since the rest is garbage for us
        const response = JSON.parse(data);
        return response.data;
    },
});

export default i18n
  .use(xhr)
  .use(lngDetector)
  .init({
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

  });
