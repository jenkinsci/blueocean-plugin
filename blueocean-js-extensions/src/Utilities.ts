/**
 * Common interfaces to this module
 */

/**
 * Provider function, must return something, when called with no arguments
 */
export interface Provider {
    (): any;
}

export interface Map<T> {
    [key: string]: T
}

export interface Constructor {
    new(... any: any[]): Object;
}

export interface NestedConstructor {
    new(... any: any[]): Object;
    [key: string]: NestedConstructor
}

export interface Logger {
    (... any: any[]): void;
}

export function getIdentifier(o: any): string {
    let registryKey: string;
    if (o instanceof String || typeof o === 'string') {
        registryKey = <string>o;
    } else {
        if (o.constructor) {
            registryKey = o.constructor.name;
        }
        if (o.name) {
            registryKey = o.name;
        }
    }
    if (registryKey) {
        return registryKey;
    }
    throw new Error('unable to determine identifier for: ' + o);
}
