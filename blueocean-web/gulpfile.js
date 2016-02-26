//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('jenkins-js-builder');

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .generateNoImportsBundle();

