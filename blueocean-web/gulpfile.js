//
// See https://github.com/jenkinsci/js-builder
//
var gi = require('giti');
var fs = require('fs');
var builder = require('@jenkins-cd/js-builder');

// create a dummy revisionInfo so developmentFooter will not fail
const revisionInfo = '// Do not edit, it is generated and will be on each build.\nexport default {};';
fs.writeFile('target/classes/io/jenkins/blueocean/revisionInfo.js', revisionInfo, err => {
  if (err) throw err;
});
gi(function (err, result) {
    if (err) return console.log(err);
    result.timestamp = new Date().toISOString();
    const revisionInfo = '/* eslint-disable */\n// Do not edit, it is generated and will be on each build.\nexport default ' + JSON.stringify(result);
    fs.writeFile('target/classes/io/jenkins/blueocean/revisionInfo.js', revisionInfo, err => {
        if (err) {
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
