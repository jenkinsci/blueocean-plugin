import { Provider, Constructor } from './Utilities';

export class Injector {
    private typeRegistry: { [id: number]: Provider } = {};
    private typeList: any[] = [];
    private singletonInstanceRegistry: { [id: number]: any } = {};
    private debugLog: boolean | any = false;

    setDebug(debug: boolean) {
        this.debugLog = debug ? console.log : false;
    }

    getId(key: any): number {
        let index = this.typeList.indexOf(key);
        if (index === -1) {
            index = this.typeList.push(key) - 1;
        }
        return index;
    }

    /**
     * Property decorator
     */
    injectPropertyDecorator(...args: any[]): any {
        if (args.length === 0) {
            throw new Error('@Inject not supported without parameters, use @Inject(<type>)');
        }

        const constructor = args[0];
        if (this.debugLog) this.debugLog('INJECT:', constructor, 'With args: ', args);

        const id = this.getId(constructor);
        return function(target: Object, propertyKey: string | symbol) {
            Object.defineProperty(target, propertyKey.toString(), {
                get: function() {
                    if (this.debugLog) this.debugLog('INJECTING:', constructor, 'propertyKey: ', propertyKey);

                    const provider: Provider = this.typeRegistry[id];
                    return !provider ? undefined : provider();
                }
            });
        };
    }

    registerExternal(key: any, provider: Provider) {
        const id = this.getId(key);
        this.typeRegistry[id] = provider;
    }

    registerExternalInstance(key: any, instance: any) {
        this.registerExternal(key, () => instance);
    }

    singletonClassDecorator(constructor: Constructor): any {
        console.log('EXTERNAL:', constructor, 'With args: ', arguments);

        const id = this.getId(constructor);
        this.registerExternal(constructor, () => {
            const instance = this.singletonInstanceRegistry[id]
                || (this.singletonInstanceRegistry[id] = new (constructor)());
            return instance;
        });

        return constructor;
    }

    /**
     * Registers an externally provided type
     */
    externalClassDecorator(constructor: Constructor): any {
        this.registerExternal(constructor, () => constructor);
        return constructor;
    }
}

const injector = new Injector();

export const Inject = injector.injectPropertyDecorator.bind(injector);
export const Singleton = injector.singletonClassDecorator.bind(injector);
export const External = injector.externalClassDecorator.bind(injector);

export default injector;
