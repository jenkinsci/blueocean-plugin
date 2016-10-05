import i18n from 'i18next';

i18n
  .init({
    fallbackLng: 'en',

    // have a common namespace used around the full app
    ns: ['common'],
    defaultNS: 'common',

    debug: true,

    interpolation: {
      escapeValue: false // not needed for react!!
    },
    lng: 'en',
    resources: {
      en: {
        translation: {
          "key": "hello world"
        }
      }
    }
  });

export default i18n;
