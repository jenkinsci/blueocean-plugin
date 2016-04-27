//
// See https://github.com/jenkinsci/js-builder
//
var gi = require('giti');
var fs = require('fs');
var builder = require('@jenkins-cd/js-builder');
// Or Array of Strings
gi(['name', 'branch', 'sha', 'author' ], function(err, result) {
  result.timestamp = new Date().toISOString();
  const revisionInfo = '/* eslint-disable */\n// Do not edit, is generate\nexport default ' + JSON.stringify(result);
  fs.writeFile('src/main/js/revisionInfo.js', revisionInfo, err => {
    if(err) {
        return console.log(err);
    }

    console.log("The file was saved!\n" + revisionInfo);
  })
});
// Explicitly setting the src paths in order to allow the rebundle task to
// watch for changes in the JDL (js, css, icons etc).
// See https://github.com/jenkinsci/js-builder#setting-src-and-test-spec-paths
builder.src(['src/main/js', 'src/main/less', 'node_modules/@jenkins-cd/design-language/dist']);

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .generateNoImportsBundle();
