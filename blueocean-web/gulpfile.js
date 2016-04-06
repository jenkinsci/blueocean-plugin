//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('@jenkins-cd/js-builder');

// Explicitly setting the src paths in order to allow the rebundle task to
// watch for changes in the JDL (js, css, icons etc).
// See https://github.com/jenkinsci/js-builder#setting-src-and-test-spec-paths
builder.src(['src/main/js', 'src/main/less', 'node_modules/@jenkins-cd/design-language/dist']);

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    // .withExternalModuleMapping('jquery-detached', 'jquery-detached:jquery2')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .generateNoImportsBundle();
