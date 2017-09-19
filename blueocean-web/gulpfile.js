//
// See https://github.com/jenkinsci/js-builder
//

process.env.SKIP_BLUE_IMPORTS = 'YES';

var gi = require('giti');
var fs = require('fs');

var builder = require('@jenkins-blueocean/js-builder');

// create a dummy revisionInfo so developmentFooter will not fail
const revisionInfo = '// Do not edit, it is generated and will be on each build.\nexport default {};';

// Create the dir path. This gets executed by mvn before the
// Java src is compiled, so it's not already created for
// the revisionInfo stuff below to work without write failures.
builder.paths.mkdirp('target/classes/io/jenkins/blueocean');

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
    });
});
// Explicitly setting the src paths in order to allow the rebundle task to
// watch for changes in the JDL (js, css, icons etc).
// See https://github.com/jenkinsci/js-builder#setting-src-and-test-spec-paths
builder.src([
    'src/main/js',
    'src/main/less']);

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .onStartup('./src/main/js/init')
    .export('redux')
    .export('mobx-react')
    .export("immutable")
	.export("keymirror")
    .export("react-redux")
    .export("redux-thunk")
    .import('react@any', {
        aliases: ['react/lib/React'] // in case a module requires react through the back door
    })
    .import('react-dom@any')
    .import('mobx@any')
    .import("@jenkins-cd/js-extensions@any")
    .import("@jenkins-cd/blueocean-core-js@any")
    .import('@jenkins-cd/design-language@any')
    .generateNoImportsBundle();

//
// An Internet Explorer specific polyfill.
// Go IE ... you never fail to make me smile :(
//
builder.bundle('src/main/js/ie/iepolyfills.js');
