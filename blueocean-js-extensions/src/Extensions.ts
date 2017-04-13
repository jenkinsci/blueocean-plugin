/**
 * ExtensionPoint registry
 */

import { Provider, Logger, Map, Constructor, NestedConstructor, getIdentifier } from './Utilities';

export interface ExtensionDescriptor {
    ordinal: number;
    provider: Provider;
    extension: any;
    extensionName: string;
    insertionOrder: number;
}

/**
 * Interface for change listeners
 */
export interface ExtensionListChangeListener {
    (extensionPoint: any, extensionDescriptorList: ExtensionDescriptor[], extension: any): void;
}

/**
 * Interface for change listeners
 */
export interface ExtensionPointChangeListener {
    (extensionPointName: any, extensionPoint: any, initializing: boolean): void;
}

export class ExtensionRegistry {
    protected extensionPoints: Map<NestedConstructor> = {};
    protected extensionProviders: Map<ExtensionDescriptor[]> = {};
    protected instanceRegistry: Map<any> = {};
    protected currentModuleName: string[] = [];
    protected extensionListeners: ExtensionListChangeListener[] = [];
    protected extensionPointListeners: ExtensionPointChangeListener[] = [];
    protected extensions: Map<NestedConstructor> = {};

    private debugLog: Logger = undefined;
    private traceLog: Logger = undefined;

    setDebug(enabled: boolean) {
        this.debugLog = enabled ? console.log : undefined;
    }

    setTrace(enabled: boolean) {
        this.traceLog = enabled ? console.log : undefined;
    }

    /**
     * Should typically only be used by test frameworks
     */
    _reset() {
        if (this.debugLog) this.debugLog('Resetting ExtensionRegistry from', new Error().stack);
        const removeAllKeys = (o: any) => {
            for (var k in o) {
                delete o[k];
            }
        }
        removeAllKeys(this.extensionPoints);
        removeAllKeys(this.extensionProviders);
        removeAllKeys(this.instanceRegistry);
    }

    pushCurrentModuleName(name: string) {
        this.currentModuleName
    }

    getExtensionPoints() {
        return this.extensionPoints;
    }

    getModuleName(): string {
        const moduleName = this.currentModuleName[this.currentModuleName.length-1];
        if (this.traceLog) this.traceLog('getModuleName:', moduleName);
        return moduleName;
    }

    getRegisteredExtensions(): Map<any> {
        return this.extensions;
    }

    /**
     * Returns all the extension types for the given extension point
     */
    getExtensions(extensionPoint: any): any[] {
        const id = getIdentifier(extensionPoint);
        const providers = this.getExtensionProviders(id, extensionPoint, true);
        if (this.traceLog) this.traceLog('Looking for extensions', id, 'with', providers);
        return providers.map(extension => extension.extension);
    }

    /**
     * Returns all the extension instances for the given extension point
     */
    getExtensionInstances(extensionPoint: any): any[] {
        const id = getIdentifier(extensionPoint);
        const providers = this.getExtensionProviders(id, extensionPoint, true);
        if (this.traceLog) this.traceLog('Looking for extension instances', id, 'with', providers);
        return providers.map(extension => extension.provider());
    }

    /**
     * Registers a class-based extension point
     */
    registerTypedExtensionPoint(extensionPoint: any) {
        const extensionName = getIdentifier(extensionPoint);
        const moduleName = this.getModuleName();
        this.registerExtensionPoint(moduleName, extensionName, extensionPoint);
    }

    /**
     * Register an extension point
     */
    registerExtensionPoint(moduleName: string, extensionName: string, extensionPoint: any): void {
        if (this.debugLog) this.debugLog("registerExtensionPoint", extensionName);
        if (moduleName) {
            const moduleExtensionPoints = this.extensionPoints[moduleName] || (this.extensionPoints[moduleName] = <NestedConstructor>{});
            moduleExtensionPoints[extensionName] = extensionPoint;
        }
        if (this.extensionPoints[extensionName]) {
            throw new Error('Extension: "' + extensionName + '" already registered');
        }
        this.extensionPoints[extensionName] = extensionPoint;
        this.extensionPointListeners.map(l => l(extensionName, extensionPoint, false));
    }

    /**
     * Class decorator takes a Function
     */
    extensionPointClassDecorator(constructor: any): any {
        this.registerTypedExtensionPoint(constructor);
    }

    /**
     * Called to directly register an extension based on the extensionPointId
     */
    registerTypedExtension(extensionPoint: Constructor, extension: Constructor, provider: Provider, ordinal?: number) {
        const extensionPointId: string = getIdentifier(extensionPoint);
        const identifier: string = getIdentifier(extension);
        this.registerExtension(extensionPointId, identifier, extension, provider, ordinal);
    }

    /**
     * Called to directly register an extension based on the extensionPointId
     */
    registerExtension(extensionPointId: string, identifier: string, extension: any, provider: Provider, ordinal?: number) {
        const types = this.extensionProviders[extensionPointId] || (this.extensionProviders[extensionPointId] = []);

        const descriptor: ExtensionDescriptor = {
            ordinal: ordinal,
            extensionName: identifier,
            insertionOrder: types.length,
            extension: extension,
            provider: provider,
        };

        // Add this extension with a provider method based on
        // the constructor
        types.push(descriptor);

        // Sort based on ordinal, insertion order?
        types.sort((a, b) => {
            // no ordinal is last
            if (a.ordinal !== undefined || b.ordinal !== undefined) {
                if (b.ordinal === undefined) {
                    return -1;
                }
                if (a.ordinal === undefined) {
                    return 1;
                }
                if (a.ordinal < b.ordinal) {
                    return -1;
                }
                if (a.ordinal > b.ordinal) {
                    return 1;
                }
            }
            // priorities are equal or undefined
            return a.extensionName.localeCompare(b.extensionName);
        });

        // Allow requested extensions to be returned on demand
        Object.defineProperty(this.extensions, identifier, {
            configurable: true,
            get: () => extension,
        });

        // Notify all listeners of this change
        this.extensionListeners.map(l => l(extensionPointId, types, descriptor));

        if (this.debugLog) this.debugLog('registerExtension for extensionPoint:', extensionPointId, 'named:', identifier, ', now has types: ', types.map(o => o.extensionName));
    }

    getExtensionProviders(extensionName: string, extensionPoint: any, initialize: boolean): ExtensionDescriptor[] {
        let providers = this.extensionProviders[extensionName];
        if (!providers) {
            providers = this.extensionProviders[extensionName] = [];
        }
        if (!extensionPoint.initialized) {
            // Extension points requested, initialized
            this.extensionPointListeners.map(l => l(extensionName, extensionPoint, true));
            extensionPoint.initialized = true;
        }
        return providers;
    }

    serviceClassDecorator(constructor: any): any {
        const extensionName = getIdentifier(constructor);
        if (this.debugLog) this.debugLog('@Service:', extensionName, 'With constructor: ', constructor);
        // is the argument an extension point?
        let superType = constructor;
        const ordinal = constructor.ordinal;
        try {
            while (superType && superType !== Function && superType !== Object) {
                const supertypeName = getIdentifier(superType);
                if (this.debugLog) this.debugLog('extensionClassDecorator registering', extensionName, ' with superclass:', supertypeName);
                this.registerTypedExtension(superType, constructor, () => new (constructor)(), ordinal);
                superType = Object.getPrototypeOf(superType.prototype).constructor;
            }
        } catch(e) {
            console.error('Unable to determine supertype for: ', constructor, 'due to', e);
        }

        return constructor;
    }

    extensionClassDecorator(...args: any[]): any {
        // Deal with multiple call styles:
        // @Extension class Something extends ExtensionPointName { ... }
        // @Extension(ExtensionPointName) class Something { ... }
        // decorators for some reason are called directly,
        // the challenge here is that we do not know the call
        // style since either way will get a constructor instance, so just
        // test if the argument is a known extension point
        const extensionDecorator = (constructor: any) => {
            const extensionName = getIdentifier(constructor);
            if (this.debugLog) this.debugLog('Extension:', extensionName, 'With args: ', args);
            // is the argument an extension point?
            let superType = args[0] !== constructor ? args[0] : constructor;
            const ordinal = constructor.ordinal;
            try {
                while (superType && superType !== Function && superType !== Object) { //typeof(superType) !== 'object') {
                    const supertypeName = getIdentifier(superType);
                    if (this.debugLog) this.debugLog('extensionClassDecorator registering', extensionName, ' with superclass:', supertypeName);
                    this.registerTypedExtension(superType, constructor, () => new (constructor)(), ordinal);
                    superType = Object.getPrototypeOf(superType.prototype).constructor;
                }
            } catch(e) {
                console.error('Unable to determine supertype for: ', constructor, 'due to', e);
            }

            return constructor;
        };

        if (this.debugLog) this.debugLog('extensionClassDecorator:', args);
        if (args.length === 1) {
            // is an extension point provided?
            const extensionOrPoint = args[0];
            const extensionPointId = getIdentifier(extensionOrPoint);
            if (this.extensionPoints[extensionPointId]) {
                return extensionDecorator;
            }
            return extensionDecorator(extensionOrPoint);
        }
        return extensionDecorator;
    }

    extensionTypesPropertyDecorator(...args: any[]): any {
        if (this.debugLog) this.debugLog('extensionTypesPropertyDecorator with args', args);

        if(args.length === 1) {
            // Handle @ExtensionList(ExtensionPointName) propName...
            const constructor = args[0];
            const name = getIdentifier(constructor);

            if (this.debugLog) this.debugLog('extensionList:', name, 'With args: ', arguments);
            return (...args: any[]): any => {
                const target: Object = args[0];
                const propertyKey: string | symbol = args[1];
                const descriptor: any = args[2];
                const getter = () => {
                    const types: ExtensionDescriptor[] = this.getExtensionProviders(name, constructor, true);
                    if (this.debugLog) this.debugLog('inject extensionList to:', target, '.', propertyKey, 'With types: ', types);
                    return types.map((t: ExtensionDescriptor) => t.extension);
                };
                if (args.length === 2 || !descriptor) { // Typescript decorator
                    Object.defineProperty(target, propertyKey.toString(), {
                        get: getter,
                    });
                    return null;
                }
                else { // Babel decorator
                    descriptor.get = getter;
                    descriptor.initializer = undefined; // so Object.defineProperty is actually called
                    return descriptor;
                }
            };
        }

        throw new Error('Unsupported ExtensionList call, must use the form: @ExtensionList(<extensionPoint>)');
    }

    injectSingleExtensionPropertyDecorator(...args: any[]): any {
        if (this.debugLog) this.debugLog('injectSingleExtensionPropertyDecorator with args', args);

        if(args.length === 1) {
            // Handle @ExtensionList(ExtensionPointName) propName...
            const constructor = args[0];
            const name = getIdentifier(constructor);

            if (this.debugLog) this.debugLog('extensionList:', name, 'With args: ', arguments);
            return (...args: any[]): any => {
                const target: Object = args[0];
                const propertyKey: string | symbol = args[1];
                const descriptor: any = args[2];
                const getter = () => {
                    const types: ExtensionDescriptor[] = this.getExtensionProviders(name, constructor, true);
                    if (this.debugLog) this.debugLog('inject extension to:', target, '.', propertyKey, 'With types: ', types);
                    if (types.length > 0) {
                        return types[0].provider();
                    }
                    return types.map((t: ExtensionDescriptor) => t.provider());
                };
                if (args.length === 2 || !descriptor) { // Typescript decorator
                    Object.defineProperty(target, propertyKey.toString(), {
                        get: getter,
                    });
                    return null;
                }
                else { // Babel decorator
                    descriptor.get = getter;
                    descriptor.initializer = undefined; // so Object.defineProperty is actually called
                    return descriptor;
                }
            };
        }

        throw new Error('Unsupported ExtensionList call, must use the form: @ExtensionList(<extensionPoint>)');
    }

    injectExtensionsPropertyDecorator(...args: any[]): any {
        if (this.debugLog) this.debugLog('injectExtensionsPropertyDecorator with args', args);

        if(args.length === 1) {
            // Handle @ExtensionList(ExtensionPointName) propName...
            const constructor = args[0];
            const name = getIdentifier(constructor);

            if (this.debugLog) this.debugLog('extensionList:', name, 'With args: ', arguments);
            return (...args: any[]): any => {
                const target: Object = args[0];
                const propertyKey: string | symbol = args[1];
                const descriptor: any = args[2];
                const getter = () => {
                    const types: ExtensionDescriptor[] = this.getExtensionProviders(name, constructor, true);
                    if (this.debugLog) this.debugLog('inject extensions to:', target, '.', propertyKey, 'With types: ', types);
                    return types.map((t: ExtensionDescriptor) => t.provider());
                };
                if (args.length === 2 || !descriptor) { // Typescript decorator
                    Object.defineProperty(target, propertyKey.toString(), {
                        get: getter,
                    });
                    return null;
                }
                else { // Babel decorator
                    descriptor.get = getter;
                    descriptor.initializer = undefined; // so Object.defineProperty is actually called
                    return descriptor;
                }
            };
        }

        throw new Error('Unsupported ExtensionList call, must use the form: @ExtensionList(<extensionPoint>)');
    }

    addExtensionListener(listener: ExtensionListChangeListener): void {
        this.extensionListeners.push(listener);
    }

    removeExtensionListener(listener: ExtensionListChangeListener): void {
        const idx = this.extensionListeners.indexOf(listener);
        this.extensionListeners.splice(idx, 1);
    }

    addExtensionPointListener(listener: ExtensionPointChangeListener): void {
        this.extensionPointListeners.push(listener);
    }

    removeExtensionPointListener(listener: ExtensionPointChangeListener): void {
        const idx = this.extensionPointListeners.indexOf(listener);
        this.extensionPointListeners.splice(idx, 1);
    }
}

/**
 * Defines a ordinal on a type
 */
export function Ordinal(ordinal: number): any {
    return function(constructor: any): any {
        if (this.traceLog) this.traceLog('defining ordinal', ordinal, 'on', getIdentifier(constructor));
        constructor.ordinal = ordinal;
        return constructor;
    }
}

/**
 * This provides an easy way to get started, with a global instance
 */
const extensionRegistry = new ExtensionRegistry();

/**
 * A key => value object of extension points, TODO: organized by module
 */
export const extensionPoints: Map<NestedConstructor> = extensionRegistry.getExtensionPoints();

/**
 * A key => value object of extensions, TODO: organized by module
 */
export const extensions: Map<NestedConstructor> = extensionRegistry.getRegisteredExtensions();

/**
 * Class decorator to register an extension point
 */
export const ExtensionPoint = extensionRegistry.extensionPointClassDecorator.bind(extensionRegistry);

/**
 * Class decorator to register an extension implementation for an extension point
 */
export const Extension = extensionRegistry.extensionClassDecorator.bind(extensionRegistry);

/**
 * Property decorator to get all instances of extensions
 */
export const ExtensionList = extensionRegistry.injectExtensionsPropertyDecorator.bind(extensionRegistry);

/**
 * Property decorator to get all instances of extensions
 */
export const ExtensionTypes = extensionRegistry.extensionTypesPropertyDecorator.bind(extensionRegistry);

/**
 * Property decorator to get the first instance of the given extension
 */
export const Inject = extensionRegistry.injectSingleExtensionPropertyDecorator.bind(extensionRegistry);

/**
 * Class decorator to register this class as an extension of itself
 */
export const Service = extensionRegistry.serviceClassDecorator.bind(extensionRegistry);

/**
 * The global extension registry
 */
export default extensionRegistry;
