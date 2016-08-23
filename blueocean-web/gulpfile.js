//
// See https://github.com/jenkinsci/js-builder
//

process.env.SKIP_BLUE_IMPORTS = 'YES';

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
    .export("@jenkins-cd/js-extensions")
    .export("@jenkins-cd/design-language")
    .export('react')
    .export('react-dom')
    .export('redux')
    .export('react-addons-css-transition-group') // Have to export this because it dips down into the react package internals grrr
    .generateNoImportsBundle();

//
// Create the "Try Blue Ocean" Javascript bundle.
// This .js bundle will be added to every classic Jenkins page
// via a PageDecorator. Using this as a way of enticing Jenkins
// users to move from classic Jenkins to Blue Ocean where possible.
//
builder.bundle('src/main/js/try.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .import('jquery-detached', 'core-assets/jquery-detached:jquery2') // Bundled in Jenkins 2.x
    .less('src/main/less/try.less');

// 
// Copy/link the JDL assests into the webapp dir, making them available at runtime.
// 
var isWindows = /^win/.test(process.platform);
var assetsDstPath = './src/main/webapp/assets';
if (isWindows) {
    var assestsCopyDone = false;
    builder.onPreBundle(function() {
        if (!assestsCopyDone) {
            assestsCopyDone = true;
            var ncp = require('ncp').ncp;

            // wipe the destination directory and recreate.
            if (fs.existsSync(assetsDstPath)) {
                rmdir(assetsDstPath);
            } 
            fs.mkdirSync(assetsDstPath);
            // copy assets from stc to dsy.
            var assetsSrcPath = './node_modules/@jenkins-cd/design-language/dist/assets';
            ncp(assetsSrcPath, assetsDstPath, function (err) {
                if (err) {
                    return logger.logError(err);
                }
            });
        }
    });
} else if (!fs.existsSync(assetsDstPath)) {
    // Just need a symlink for non-windows platforms.
    var assetsSrcPath = '../../../node_modules/@jenkins-cd/design-language/dist/assets';
    fs.symlinkSync(assetsSrcPath, assetsDstPath);
}

function rmdir(path) {
    if (fs.existsSync(path)) {
        fs.readdirSync(path).forEach(function (file) {
            var curPath = path + "/" + file;
            if (fs.lstatSync(curPath).isDirectory()) {
                rmdir(curPath);
            } else {
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(path);
    }
}