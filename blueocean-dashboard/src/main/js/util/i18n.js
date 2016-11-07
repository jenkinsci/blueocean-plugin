import { I18n } from "@jenkins-cd/blueocean-core-js";
import EventEmitter from 'events';

export default class I18nHelper extends EventEmitter {

    constructor(namespaces = I18n.options.defaultNS, options = {}) {
        super();
        const { wait = true, bindI18n = 'languageChanged loaded', bindStore = 'added removed' } = options;
        this.t = I18n.getFixedT(I18n.language, namespaces);
        this.locale = I18n.language;
        this.unmount = () => {
            if (this.onI18nChanged) {
                bindI18n.split(' ').forEach((event) => {
                    I18n.off(event, this.onI18nChanged);
                });
                bindStore.split(' ').forEach((event) => {
                    I18n.store.off(event, this.onI18nChanged);
                });
            }
            this.mounted = false;
        };

        this.onI18nChanged = () => {
            if (!this.mounted) return;
            this.emit('I18nChanged',  new Date());
        };

        this.on('I18nChanged', (date) => {
           console.log('I18nChanged', date);
        });

        const bind = () => {
            bindI18n && I18n.on(bindI18n, this.onI18nChanged);
            bindStore && I18n.store && I18n.store.on(bindStore, this.onI18nChanged);
        };

        I18n.loadNamespaces(namespaces, () => {
            this.mounted = true;
            if (wait) bind();
        });

        if (!wait) bind();
    }
}
