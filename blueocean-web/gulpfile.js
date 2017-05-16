//
// See https://github.com/jenkinsci/js-builder
//

process.env.SKIP_BLUE_IMPORTS = 'YES';

var gi = require('giti');
var fs = require('fs');

var builder = require('@jenkins-cd/js-builder');

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
    'src/main/less',
    'node_modules/@jenkins-cd/design-language/dist',
    'node_modules/@jenkins-cd/blueocean-core-js/dist']);

//
// Create the main "App" bundle.
// generateNoImportsBundle makes it easier to test with zombie.
//
builder.bundle('src/main/js/blueocean.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/main/less/blueocean.less')
    .onStartup('./src/main/js/init')
    .export("@jenkins-cd/blueocean-core-js")
    .export('@jenkins-cd/blueocean-core-js/dist/js/i18n/bundle-startup') // remove once JENKINS-39646 fixes back-door bundle module leakage
    .export("@jenkins-cd/design-language")
    .export("@jenkins-cd/js-extensions")
    .export('react')
    .export('react-dom')
    .export('redux')
    .export('mobx')
    .export('mobx-react')
    .generateNoImportsBundle();

//
// An Internet Explorer specific polyfill.
// Go IE ... you never fail to make me smile :(
//
builder.bundle('src/main/js/ie/iepolyfills.js');

// Copy/link library assests into the src/main/webapp/assets dir, making them available at runtime.
linkAssets('jdl', '@jenkins-cd/design-language/dist/assets');
linkAssets('corejs', '@jenkins-cd/blueocean-core-js/dist/assets');

/**
 * Link (or copy) the specified module's subdir to a dir within /src/main/webapp/assets
 * @param {string} dirName name of directory link
 * @param {string} modulePath path within module, e.g. '@org-name/module/name/some/path
 */
function linkAssets(dirName, modulePath) {
    var isWindows = /^win/.test(process.platform);
    var assetsDstPath = './src/main/webapp/assets/' + dirName;

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
                var assetsSrcPath = './node_modules/' + modulePath;
                ncp(assetsSrcPath, assetsDstPath, function (err) {
                    if (err) {
                        return logger.logError(err);
                    }
                });
            }
        });
    } else if (!fs.existsSync(assetsDstPath)) {
        // Just need a symlink for non-windows platforms.
        var assetsSrcPath = '../../../../node_modules/' + modulePath;
        fs.symlinkSync(assetsSrcPath, assetsDstPath);
    }
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
