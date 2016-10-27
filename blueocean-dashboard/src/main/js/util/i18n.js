import { i18n } from "@jenkins-cd/blueocean-core-js";
import EventEmitter from 'events';

export default class I18n extends EventEmitter {

    constructor(namespaces = i18n.options.defaultNS, options = {}) {
        super();
        const { wait = true, bindI18n = 'languageChanged loaded', bindStore = 'added removed' } = options;
        this.t = i18n.getFixedT(i18n.language, namespaces);
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
           console.log('i18nChanged', date);
        });

        const bind = () => {
            bindI18n && i18n.on(bindI18n, this.onI18nChanged);
            bindStore && i18n.store && i18n.store.on(bindStore, this.onI18nChanged);
        };

        i18n.loadNamespaces(namespaces, () => {
            this.mounted = true;
            if (wait) bind();
        });

        if (!wait) bind();
    }
}
