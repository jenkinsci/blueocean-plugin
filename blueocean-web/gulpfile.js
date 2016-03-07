//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('@jenkins-cd/js-builder');

// Disable js-builder based linting for now.
// Will get fixed with https://github.com/cloudbees/blueocean/pull/55
builder.lint('none');

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    .withExternalModuleMapping('jquery-detached', 'jquery-detached:jquery2')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .generateNoImportsBundle();

