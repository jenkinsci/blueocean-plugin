export function getPropTypes(obj) {
    if (obj.propTypes) {
        return obj.propTypes;
    }
    if (obj.prototype) {
        return getPropTypes(obj.prototype);
    }
}

export function validateExtensionProps(extension) {
    const propTypes = getPropTypes(extension);
    if (propTypes) {
        for (const prop of Object.keys(propTypes)) {
            if (prop.isRequired && !(prop in extension)) {
                console.error('missing required prop', prop, extension);
                throw new Error('missing required prop ' + prop + ' in extension: ' + extension);
            }
        }
    }
}
