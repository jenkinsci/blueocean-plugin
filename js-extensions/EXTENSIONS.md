# Jenkins JavaScript Extensions

Jenkins JavasScript Extensions (JSe) in BlueOcean are handled in the UI by the `@jenkins-cd/js-extensions` module.

JSe is based on the extensibility model already established by Jenkins, based on data and views, with the ability to inherit views based on parent data types.

JSe `@jenkins-cd/js-extensions` module exports 2 things:
- `store` - the `ExtensionStore` instance, which must be initialized
- `Renderer` - a React component to conveniently render extensions

### Store API

The `ExtensionStore` API is very simple, all public methods are asynchronous:

- `getExtensions(extensionPointName, [type,] onload)`
    This method will async load data and type information as needed, and call the onload handler with a list of extension exports, e.g. the React classes or otherwise exported references.

- `getTypeInfo(type, onload)`
    This will return a list of type information, from the [classes API](../blueocean-rest/README.md#classes_API), this method also handles caching results locally.

- `init()`
    Required to be called with `{ extensionDataProvider: ..., typeInfoProvider: }` see: [ExtensionStore.js](src/ExtensionStore.js#init) for details. This is currently done in [init.jsx](../blueocean-web/src/main/js/init.jsx), with methods to fetch extension data from `<jenkins-url>/blue/js-extensions/` and type information from `<jenkins-url>/blue/rest/classes/<class-name>`.

### Rendering extension points

The most common usage pattern is to use the exported `Renderer`, specifying the extension point name, any necessary contextual data, and optionally specifying a data type.

    import Extensions from '@jenkins-cd/js-extensions';
    ...
    <Extensions.Renderer extensionPoint="jenkins.navigation.top.menu" />

For example, rendering the test results for a build may be scoped to the specific type of test results in this manner:

    <Extensions.Renderer extensionPoint="test-results-view" dataType={data._class} testResults={data} />

The `ExtensionRenderer` component optionally uses the [classes API](../blueocean-rest/README.md#classes_API) to look up an appropriate, specific set of views for the data being displayed. This should works seamlessly with other [capabilities](../blueocean-rest/README.md#capabilities).


### Defining extension points

Extensions are defined in a `jenkins-js-extensions.yaml` file in the javascript source directory of a plugin by defining a list of extensions similar to this:

    # Extensions in this plugin
    extensions:
      - component: AboutNavLink
        extensionPoint: jenkins.topNavigation.menu
      - component: components/tests/AbstractTestResult
        extensionPoint: jenkins.test.result
        type: hudson.tasks.test.AbstractTestResultAction

Properties are:
- `component`: a module from which the default export will be used
- `extensionPoint`: the extension point name
- `type`: an optional data type this extension handles

For example, the `AboutNavLink` might be defined as a default export:

    export default class NavLink extends React.Component {
        ...
    }

Although extensions are not limited to React components, this is the typical usage so far.
