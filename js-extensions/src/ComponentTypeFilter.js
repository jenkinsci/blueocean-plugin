/**
 * Tries to determine if the objectToTest is of the given type.
 * Will normalize things like String/'string' inconsistencies
 * as well as ES6 class & traditional prototype inheritance.
 * NOTE: This ALSO tests the prototype hierarchy if objectToTest
 * is a Function.
 */
export function isType(objectToTest, type) {
    var o = objectToTest;
    if (typeof o === type) {
        return true;
    }
    if (type === String || type === 'string') {
        return o instanceof String;
    }
    if (type === Function || type === 'function') {
        return o instanceof Function;
    }
    if (type === Object || type === 'object') {
        return o instanceof Object;
    }
    if (objectToTest instanceof Function) {
        var proto = objectToTest;
        while (proto) {
            if (proto === type) {
                return true;
            }
            proto = Object.getPrototypeOf(proto);
        }
    }
    return objectToTest instanceof type;
}

export function componentType(componentType) {
    return (extensions, onload) => {
        extensions = extensions.filter(e => isType(e.instance, componentType));
        onload(extensions);
    };
}
