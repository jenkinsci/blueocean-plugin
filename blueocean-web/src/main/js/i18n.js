import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend-thor';

const lngDetector = new LngDetector(null, {
  // order and from where user language should be detected
  order: ['querystring', 'navigator'],
  // keys or params to lookup language from
  lookupQuerystring: 'language',
});

const xhr = new XHR(null, {
  loadPath: '/jenkins/i18n/resourceBundle?language=##lng##&baseName=##ns##',
  allowMultiLoading: false,
  parse: function (data) {
    const response = JSON.parse(data);
    return response.data;
  },
});

i18n
  .use(xhr)
  .use(lngDetector)
  .init({
    fallbackLng: 'en',
    // have a common namespace used around the full app
    ns: ['org.jenkinsci.plugins.blueocean.web.Messages', 'hudson.logging.Messages'],
    defaultNS: 'org.jenkinsci.plugins.blueocean.web.Messages',
    preload: ['en', 'de'],
    keySeparator: '#',
    debug: true,
    load: 'all',
    interpolation: {
      prefix: '##',
      suffix: '##',
      escapeValue: false // not needed for react!!
    },

  });

export default i18n;
