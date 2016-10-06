import i18n from 'i18next';
import LngDetector from 'i18next-browser-languagedetector';

const lngDetector = new LngDetector(null, {
  // order and from where user language should be detected
  order: ['querystring', 'navigator'],
  // keys or params to lookup language from
  lookupQuerystring: 'lng',
});
 console.log(lngDetector, 'oooooooo')
i18n
  .use(lngDetector)
  .init({
    fallbackLng: 'en',
    // have a common namespace used around the full app
    ns: ['common'],
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
