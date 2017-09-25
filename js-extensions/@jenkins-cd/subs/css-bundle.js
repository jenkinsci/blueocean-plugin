/**
 * Bundle CSS/LESS for the plugin.
 */

const extensionsLESSFile = 'src/main/less/extensions.less';
var fs = require('fs');

exports.bundle = function(extensionsJSON) {
    if (__builder.maven.isMavenProject) {
        var cssFile = process.cwd() + '/' + extensionsLESSFile;

        if (fs.existsSync(cssFile)) {
            const artifactId = __builder.maven.getArtifactId();
            const cssBundle = __builder.bundle(extensionsLESSFile);

            cssBundle.bundleExportNamespace = artifactId;
            extensionsJSON.extensionCSS = 'org/jenkins/ui/jsmodules/' + artifactId + '/extensions.css';
        }
    }
    return extensionsJSON;
};
