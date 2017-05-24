/**
 * Jenkins js-builder extension plugin. https://github.com/jenkinsci/js-builder
 *
 * Adds some js-extensions specific behaviour to the build.
 */

/**
 * Install the js-builder plugin. This function will be called by js-builder.
 */
exports.install = function(builder) {
    var jsBundle = require('./subs/extensions-bundle');
    var cssBundle = require('./subs/css-bundle');

    var extensionsJSON = jsBundle.bundle();
    if (extensionsJSON) {
        // Attach plugin CSS info to the extensions JSON, if there is any.
        extensionsJSON = cssBundle.bundle(extensionsJSON);

        // Save extensions JSON again.
        jsBundle.setJSExtensionsJSON(extensionsJSON);
    }

    //
    // Defaulting the NODE_ENV to "production" so as to optimize
    // bundle generation. This ensures that e.g. the react dev tools are
    // disabled by default.
    //
    // If you need a "development" bundle then build the bundle by hand,
    // supplying a "NODE_ENV" arg e.g.
    //
    //   $ gulp bundle --NODE_ENV development
    //
    // We can build this into the mvn build later if this proves to be
    // a bit painful.
    //
    process.env.NODE_ENV = builder.args.argvValue('--NODE_ENV', 'production');
    builder.onPreBundle(function (bundler) { // See https://github.com/jenkinsci/js-builder#onprebundle-listeners
        bundler.transform(require('envify'));
    });
    
    // This hack is because core-js has references to
    // blueocean-web resource bundle strings...
//    var isJenkinsCd = packageJson.name.startsWith('@jenkins-cd/');
//    var i18bundleRequired = packageJson.name == '@jenkins-cd/blueocean-core-js' || !isJenkinsCd;
    
    builder.onPreBundle(function (bundler) {
        var bundle = this;
        // Process the requires to weed out stuff we know is being provided
        bundler.transform(require('./subs/require-transform.js'), { global: true, exports: bundle.moduleExports });
    });

    builder.onSetupBundle(function(bundle, packageJson) {
        if (packageJson.name === '@jenkins-cd/blueocean-core-js') {
            bundle.onStartup('@jenkins-cd/js-extensions/dist/init/blueocean-core-js');
        } else if (!packageJson.name.startsWith('@jenkins-cd')) {
            bundle.onStartup('@jenkins-cd/js-extensions/dist/init/extension');
        }
    });

    if (!process.env.SKIP_BLUE_IMPORTS) {
        // All Blue Ocean bundles should import the following
        // modules. These are all exported from the main blueocean
        // bootstrap bundle. Think of these as being like
        // singletons within the blueocean subsystem.
        // See jenkinscd/export in blueocean-web/package.json
        builder
            .import('@jenkins-cd/js-extensions@any')
            .import('@jenkins-cd/design-language@any')
            .import("@jenkins-cd/blueocean-core-js@any")
            .import("@jenkins-cd/logging")
            .import('react@any', {
                aliases: ['react/lib/React'] // in case a module requires react through the back door
            })
            .import('react-dom@any')
            .import("react-addons-css-transition-group@any")
            .import('redux@any')
        ;
    }

    if (!packageJson.name.startsWith('@jenkins-cd')) {
        builder.import('jenkins-cd-blueocean-core-js:jenkins-cd-blueocean-core-js@any');
    }
};
