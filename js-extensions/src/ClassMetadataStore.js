/**
 * ClassMetadataStore is responsible for maintaining extension metadata
 * including type/capability info
 */
export class ClassMetadataStore {
    init(classMetadataProvider) {
        /**
         * Type info cache
         */
        this.classMetadata = {};
        
        /**
         * Fetch function for the classMetadata
         */
        this.classMetadataProvider = classMetadataProvider;

        /**
         * Onload callbacks cache. Used to ensure we don't
         * issue multiple in-parallel requests for the same
         * class metadata.
         */
        this.classMetadataOnloadCallbacks = {};
    }
    
    /**
     * Gets the type/capability info for the given data type
     */
    getClassMetadata(type, onload) {
        var classMeta = this.classMetadata[type];
        if (classMeta) {
            return onload(classMeta);
        }
        
        var callbacks = this.classMetadataOnloadCallbacks[type];
        if (!callbacks) {
            // This is the first request for this type. Initialise the
            // callback cache and then issue the request to
            // the classMetadataProvider.
            callbacks = this.classMetadataOnloadCallbacks[type] = [];
            this.classMetadataProvider(type, (data) => {
                classMeta = this.classMetadata[type] = JSON.parse(JSON.stringify(data));
                classMeta.classes = classMeta.classes || [];
                // Make sure the type itself is in the list
                if (classMeta.classes.indexOf(type) < 0) {
                    classMeta.classes = [type, ...classMeta.classes];
                }
                delete this.classMetadataOnloadCallbacks[type];

                // Notify all callbacks
                for (var i = 0; i < callbacks.length; i++) {
                    try {
                        callbacks[i](classMeta);
                    } catch (e) {
                        console.error('Unexpected Error in ClassMetadataStore onload callback function.', e);
                    }
                }
            });
        } else {
            // We already have an inflight request to get class metadata info about
            // the requested type, so nothing to do except store the onload callback.
        }
        callbacks.push(onload);
    }
    
    dataType(dataType) {
        return (extensions, onload) => {
            if (dataType && typeof(dataType) === 'object'
                    && '_class' in dataType) { // handle the common API incoming data
                dataType = dataType._class;
            }
            
            this.getClassMetadata(dataType, (currentTypeInfo) => {
                // prevent returning extensions for the given type
                // when a more specific extension is found
                var matchingExtensions = [];
                eachType: for (var typeIndex = 0; typeIndex < currentTypeInfo.classes.length; typeIndex++) {
                    // currentTypeInfo.classes is ordered by java hierarchy, including
                    // and beginning with the current data type
                    var type = currentTypeInfo.classes[typeIndex];
                    for (var i = 0; i < extensions.length; i++) {
                        var extension = extensions[i];
                        if (type === extension.dataType) {
                            matchingExtensions.push(extension);
                        }
                    }
                    // if we have this specific type handled, don't
                    // proceed to parent types
                    if (matchingExtensions.length > 0) {
                        break eachType;
                    }
                }
                onload(matchingExtensions);
            });
        };
    }

    /**
     * Returns a filtering function to only return untyped extensions
     */
    untyped() {
        return (extensions, onload) => {
            // exclude typed extensions when types not requested
            extensions = extensions.filter(m => !('dataType' in m));
            onload(extensions);
        };
    }
}

export const instance = new ClassMetadataStore();
