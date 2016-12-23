// Provide an ExtensionStore & ExtensionRenderer react component
const ExtensionStore = require('./dist/ExtensionStore.js').default;
exports.store = new ExtensionStore();

exports.Renderer = require('./dist/ExtensionRenderer.js').default;

//in lieu of DI
const ResourceLoadTracker = require('./dist/ResourceLoadTracker.js').default;
const resourceLoadTracker = new ResourceLoadTracker();
exports.store.resourceLoadTracker = resourceLoadTracker;

//Put these in statics so we can mock them for testing. Ideally they would come from React scope.
exports.Renderer.extensionStore = exports.store;
exports.Renderer.resourceLoadTracker = resourceLoadTracker;

const ClassMetadataStore = require('./dist/ClassMetadataStore.js').default;
exports.classMetadataStore = new ClassMetadataStore();

exports.SandboxedComponent = require('./dist/SandboxedComponent.js').SandboxedComponent;

exports.ErrorUtils = require('./dist/ErrorUtils.js').default;

exports.dataType = function dataType(dataType) { return exports.classMetadataStore.dataType(dataType); };

exports.untyped = function untyped() { return exports.classMetadataStore.untyped(); };

exports.isType = require('./dist/ComponentTypeFilter.js').isType;

exports.componentType = require('./dist/ComponentTypeFilter.js').componentType;

exports.init = function init(args) {
    exports.classMetadataStore.init(args.classMetadataProvider);
    exports.store.init({
        extensionData: args.extensionData,
        classMetadataStore: exports.classMetadataStore,
    });
};
