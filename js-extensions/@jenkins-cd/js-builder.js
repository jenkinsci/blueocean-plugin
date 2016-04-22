/**
 * Jenkins js-builder extension plugin. https://github.com/jenkinsci/js-builder
 *
 * Adds some js-extensions specific behaviour to the build.
 */

var dependencies = __builder.dependencies;
var maven = __builder.maven;
var paths = __builder.paths;
var logger = __builder.logger;
var fs = require('fs');
var path = require('path');
var cwd = process.cwd();

var jsExtensionsYAMLFile = findExtensionsYAMLFile();

/**
 * Install the js-builder plugin. This function will be called by js-builder.
 */
exports.install = function() {
    try {
        if (jsExtensionsYAMLFile) {
            // Transform the jenkins-js-extensions.yaml file + enrich with some info
            transformToJSON();
            // Generate a jenkins-js-extensions.jsx from the jenkins-js-extensions.yaml.
            var jsxFile = transformToJSX();
            // Generate a bundle for the extensions.
            createBundle(jsxFile);
        }
    } catch (e) {
        logger.logError(e);
    }
};

exports.readYAMLFile = function(file) {
    if (!file || !fs.existsSync(file)) {
        return undefined;
    }
    var rawYAML = fs.readFileSync(file, "utf-8");
    return require('js-yaml').load(rawYAML);
};

exports.getJSExtensions = function() {
    return exports.readYAMLFile(jsExtensionsYAMLFile);
};

exports.yamlToJSON = function(sourceFile, targetFile, transformer) {
    var asJSON = exports.readYAMLFile(sourceFile);
    if (transformer) {
        asJSON = transformer(asJSON);
    }
    fs.writeFileSync(targetFile, JSON.stringify(asJSON, undefined, 4));
};

/**
 * Find the jenkins-js-extension.yaml file in the src paths.
 */
function findExtensionsYAMLFile() {
    for (var i = 0; i < paths.srcPaths.length; i++) {
        var srcPath = path.resolve(cwd, paths.srcPaths[i]);
        var extFile = path.resolve(srcPath, 'jenkins-js-extension.yaml');
        if (fs.existsSync(extFile)) {
            return extFile;
        }
    }

    return undefined;
}

function transformToJSON() {
    // If there's a jenkins-js-extensions.yaml, transform it to jenkins-js-extensions.json
    // in the target/classes dir, making it easier to consume on the backend (HPI extension discovery).
    if (jsExtensionsYAMLFile) {
        assertHasJenkinsJsExtensionsDependency('Your project defines a jenkins-js-extensions.yaml file\n\t- Path: ' + jsExtensionsYAMLFile);
        var jsExtensionsJSONFile = cwd + '/target/classes/jenkins-js-extension.json';

        paths.mkdirp('target/classes');
        exports.yamlToJSON(jsExtensionsYAMLFile, jsExtensionsJSONFile, function(json) {
            if (maven.isHPI()) {
                json.hpiPluginId = maven.getArtifactId();
            }
            return json;
        });
    }
}

function transformToJSX() {
    // If there's a jenkins-js-extensions.yaml, transform it to jenkins-js-extensions.jsx
    if (jsExtensionsYAMLFile) {
        assertHasJenkinsJsExtensionsDependency('Your project defines a jenkins-js-extensions.yaml file\n\t- Path: ' + jsExtensionsYAMLFile);

        var extensionsMeta = exports.getJSExtensions();

        if (!extensionsMeta || !extensionsMeta.extensions || extensionsMeta.extensions.length === 0) {
            return undefined;
        }

        var extensions = extensionsMeta.extensions;
        var srcRoot = path.dirname(jsExtensionsYAMLFile);
        var targetRoot = cwd + '/target';
        var relPath = path.relative(targetRoot, srcRoot);
        var jsxFilePath = targetRoot + '/jenkins-js-extension.jsx';
        var jsxFileContent = '';

        jsxFileContent += "//\n";
        jsxFileContent += "// NOTE: This JSX file is generated and should NOT be added to source control.\n";
        jsxFileContent += "//\n";
        jsxFileContent += "\n";

        // Add all the top level imports...
        for (var i1 = 0; i1 < extensions.length; i1++) {
            var extension = extensions[i1];

            extension.importAs = 'component_' + i1;
            jsxFileContent += "import " + extension.importAs + " from '" + relPath + "/" + extension.component + ".jsx';\n";
        }

        // Add the js-modules import of the extensions and add the code to register all
        // of the extensions in the shared store.
        jsxFileContent += "require('@jenkins-cd/js-modules').import('jenkins-cd:js-extensions').onFulfilled(function(extensions) {\n";
        for (var i2 = 0; i2 < extensions.length; i2++) {
            var extension = extensions[i2];

            jsxFileContent += "    extensions.store.addExtension('" + extension.extensionPoint + "', " + extension.importAs + ");\n";
        }
        jsxFileContent += "});";

        fs.writeFileSync(jsxFilePath, jsxFileContent);

        return jsxFilePath;
    }

    return undefined;
}

function createBundle(jsxFile) {
    __builder.bundle(jsxFile)
        .namespace(maven.getArtifactId())
        .withExternalModuleMapping('@jenkins-cd/js-extensions', 'jenkins-cd:js-extensions')
        .withExternalModuleMapping('@jenkins-cd/design-language', 'jenkins-cd:jdl')
        .withExternalModuleMapping('react', 'react:react')
        .withExternalModuleMapping('react-dom', 'react:react-dom')
        .inDir('target/classes/org/jenkins/ui/jsmodules/' + maven.getArtifactId());
}

function hasJenkinsJsExtensionsDep() {
    return (dependencies.getDependency('@jenkins-cd/js-extensions') !== undefined);
}

function assertHasJenkinsJsExtensionsDependency(message) {
    if(!hasJenkinsJsExtensionsDep()) {
        dependencies.exitOnMissingDependency('@jenkins-cd/js-extensions', message);
    }
}
