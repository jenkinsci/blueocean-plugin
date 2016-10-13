import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend';

/**
 * Init language detector, we are going to use first queryString and then the navigator prefered language
 */
const lngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
});
/**
 * configure the backend for our locale
 */
const xhr = new XHR(null, {
    loadPath: '/jenkins/i18n/resourceBundle?language=##lng##&baseName=##ns##',
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
      ns: ['org.jenkinsci.plugins.blueocean.web.Messages'],
      defaultNS: 'org.jenkinsci.plugins.blueocean.web.Messages',
      preload: ['en', 'de'],
      keySeparator: '#',
      debug: true,
      load: 'all',
      interpolation: {
          prefix: '##',
          suffix: '##',
          escapeValue: false, // not needed for react!!
      },

  });
