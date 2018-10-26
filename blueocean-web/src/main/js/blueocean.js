import jdlCSS from '@jenkins-cd/design-language/less/theme.less';
import style from '../less/blueocean.less';
import dashboardCSS from '@jenkins-cd/blueocean-dashboard/src/main/less/extensions.less';
import coreCSS from '@jenkins-cd/blueocean-core-js/src/less/blueocean-core-js.less';
import resources from '../../../locales';
import LngDetector from 'i18next-browser-languagedetector';
import i18next from '../../../../blueocean-core-js/node_modules/i18next';

const defaultLngDetector = new LngDetector(null, {
    // order and from where user language should be detected
    order: ['querystring', 'htmlTag', 'navigator'],
    // keys or params to lookup language from
    lookupQuerystring: 'language',
    // Don't use the default (document.documentElement) because that can
    // trigger the browsers auto-translate, which is quite annoying.
    htmlTag: window.document ? window.document.head : undefined,
});

i18next.init({
    fallbackLng: 'en',
    keySeparator: false, // we do not have any nested keys in properties files
    debug: false,
    load: 'currentOnly',
    interpolation: {
        prefix: '{',
        suffix: '}',
        escapeValue: false, // not needed for react!!
    },
    resources,
});
i18next.use(defaultLngDetector);

try {
    // start the App
    require('./main.jsx');
} catch (e) {
    console.error('Error starting Blue Ocean.', e);
}
