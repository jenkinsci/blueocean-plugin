/**
 * Bundle JS extensions.
 */

var dependencies = __builder.dependencies;
var maven = __builder.maven;
var paths = __builder.paths;
var logger = __builder.logger;
var fs = require('fs');
var path = require('path');
var cwd = process.cwd();
var jsonFile = cwd + '/target/classes/jenkins-js-extension.json';
var ModuleSpec = require('@jenkins-cd/js-modules/js/ModuleSpec');

var jsExtensionsYAMLFile = findExtensionsYAMLFile();

exports.bundle = function() {
    try {
        if (jsExtensionsYAMLFile) {
            // Transform the jenkins-js-extensions.yaml file + enrich with some info
            var extensionsJSON = transformToJSON();
            // Generate a jenkins-js-extensions.jsx from the jenkins-js-extensions.yaml.
            var jsxFile = transformToJSX();
            // Generate a bundle for the extensions.
            createBundle(jsxFile);

            return extensionsJSON;
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

exports.getJSExtensionsYAML = function() {
    return exports.readYAMLFile(jsExtensionsYAMLFile);
};

exports.getJSExtensionsJSON = function() {
    return require(jsonFile);
};

exports.setJSExtensionsJSON = function(json) {
    fs.writeFileSync(jsonFile, JSON.stringify(json, undefined, 4));
};

exports.yamlToJSON = function(sourceFile, targetFile, transformer) {
    var asJSON = exports.readYAMLFile(sourceFile);
    if (transformer) {
        asJSON = transformer(asJSON);
    }
    fs.writeFileSync(targetFile, JSON.stringify(asJSON, undefined, 4));
    return asJSON;
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
    assertHasJenkinsJsExtensionsDependency('Your project defines a jenkins-js-extensions.yaml file\n\t- Path: ' + jsExtensionsYAMLFile);

    paths.mkdirp('target/classes');
    return exports.yamlToJSON(jsExtensionsYAMLFile, jsonFile, function(json) {
        if (maven.isHPI()) {
            json.hpiPluginId = maven.getArtifactId();
        }
        return json;
    });
}

function transformToJSX() {
    // If there's a jenkins-js-extensions.yaml, transform it to jenkins-js-extensions.jsx
    if (jsExtensionsYAMLFile) {
        assertHasJenkinsJsExtensionsDependency('Your project defines a jenkins-js-extensions.yaml file\n\t- Path: ' + jsExtensionsYAMLFile);

        var extensionsMeta = exports.getJSExtensionsYAML();

        if (!extensionsMeta || !extensionsMeta.extensions || extensionsMeta.extensions.length === 0) {
            return undefined;
        }

        var extensions = extensionsMeta.extensions;
        var srcRoot = path.dirname(jsExtensionsYAMLFile);
        var targetRoot = cwd + '/target';
        var relPath = path.relative(targetRoot, srcRoot).replace(/\\/g, "/");
        var jsxFilePath = targetRoot + '/jenkins-js-extension.jsx';
        var jsxFileContent = '';

        jsxFileContent += "//\n";
        jsxFileContent += "// NOTE: This JSX file is generated and should NOT be added to source control.\n";
        jsxFileContent += "//\n";
        jsxFileContent += "\n";

        // Add all the top level imports...
        for (var i1 = 0; i1 < extensions.length; i1++) {
            var extension = extensions[i1];
            var sanityCheckMessage = "The component " +
                extension.component + " does not define a default export. Imported from " +
                jsExtensionsYAMLFile + " in " + maven.getArtifactId();

            extension.importAs = 'component_' + i1;
            jsxFileContent += "import " + extension.importAs + " from '" + relPath + "/" + extension.component + ".jsx';\n";
            jsxFileContent += "if(!" + extension.importAs + ") throw new Error('" + sanityCheckMessage + "');\n";
        }

        // Add the js-modules import of the extensions and add the code to register all
        // of the extensions in the shared store.
        var jsExtensionsModuleSpec = new ModuleSpec('@jenkins-cd/js-extensions@any');
        jsxFileContent += "require('@jenkins-cd/js-modules').import('" + jsExtensionsModuleSpec.importAs() + "').onFulfilled(function(Extension) {\n";
        for (var i2 = 0; i2 < extensions.length; i2++) {
            var extension = extensions[i2];

            jsxFileContent += "    Extension.store._registerComponentInstance('" + extension.extensionPoint + "', '" + maven.getArtifactId() + "', '" + extension.component + "', " + extension.importAs + ");\n";
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
