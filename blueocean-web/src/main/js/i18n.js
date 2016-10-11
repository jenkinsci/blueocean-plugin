import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';
import XHR from 'i18next-xhr-backend-thor';

const lngDetector = new LngDetector(null, {
  // order and from where user language should be detected
  order: ['querystring', 'navigator'],
  // keys or params to lookup language from
  lookupQuerystring: 'lng',
});

const xhr = new XHR(null, {
  loadPath: '/jenkins/i18n/resourceBundle?language={{lng}}&baseName={{ns}}',
  allowMultiLoading: false,
  parse: function (data) {
    console.log('mujer', data);
    return data;
  },
});
console.log('rtrt', xhr)
i18n
  .use(xhr)
  .use(lngDetector)
  .init({
    fallbackLng: 'en',
    // have a common namespace used around the full app
    ns: ['common', 'hudson.logging.Messages'],
    defaultNS: 'common',
    preload: ['en', 'de'],
    debug: true,
    load: 'all',
    interpolation: {
      escapeValue: false // not needed for react!!
    },

    resources: {
      de: {
        common: {
          login: 'Einloggen',
          logout: 'Ausloggen',
          pipelines: "Röhren",
          administration: "Verwaltung",
        }
      },
      en: {
        common: {
          login: 'Login',
          logout: 'Logout',
          pipelines: "Pipelines",
          administration: "Administration",
        }
      }
    }
  });

export default i18n;
