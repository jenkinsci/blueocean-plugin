#!/usr/bin/env node
/**
 * This script will install the package and version you want in the directories
 * that are defined in below array. It accept a full version string
 * e.g. @jenkins-cd/design-language@0.0.105-TimePrecise
 * as input and if you do not provide that it will start a prompt.
 *
 * We will prune and install BEFORE we install the requested version to make sure that
 * shrinkwrap will update correctly everytime. We further do an optional `mvn install`
 * to publish the new hpi to the local .m2 repository
 *
 * Usage: bin/cleanInstall.js [[@scope/]package@version] [dev] [mvn]
 *
 * dev will install a devDependency
 * mvn will run the optional `mvn install` afterwards
 */
const fs = require('fs');
const async = require('async');
const exec = require('child_process').exec;
const prompt = require('prompt');

const start = new Date().getTime();
const directories = ['../blueocean-dashboard', '../blueocean-personalization', '../blueocean-web', '../blueocean-pipeline-editor'];

var isDevDependency = false; // Set via command line
var shouldRunMaven = false; // Set via command line

function invokeInstall(err, result) {
    // Log the input / command parse results.
    console.log('Command-line input received:');
    console.log('package: ' + result.package);
    console.log('version: ' + result.version);
    console.log('   type: ' + (isDevDependency ? 'dev' : 'production'));
    console.log('    mvn: ' + (shouldRunMaven ? 'will run mvn' : 'will not run mvn'));

    async.map(directories,
        function (elem, callback) {
            console.log('Current element', elem);
            removeAndInstall(elem, result.package,  result.version, callback);
        },
        function (err, result) {
            if (err) {
                console.error('Something went wrong! node_modules might now be trashed, sorry.', err);
                process.exit(1);
            } else {
                const ellapsed = new Date().getTime() - start;
                console.log(`Install look good! took ${ellapsed}ms`);
                process.exit(0);
            }
        }
    );
}

// Main

for (let i = 2; i < process.argv.length; i++) {
    let lcase = String(process.argv[i]).toLowerCase();
    if (lcase == 'mvn') {
        shouldRunMaven = true;
    } else if (lcase == 'dev') {
        isDevDependency = true;
    }
}

if (process.argv[2]) {
    const versionArray = process.argv[2].split('@');
    const result = {};
    if (versionArray.length > 2) {
        // Assuming a scoped NPM package that begins with @
        result.package = "@" + versionArray[1];
        result.version = versionArray[2];
    } else {
        result.package = versionArray[0];
        result.version = versionArray[1];
    }
    invokeInstall(null, result);
} else {
    prompt.start();
    prompt.get({
        properties: {
            package: {
                message: `PACKAGE to install?`,
                required: true,
            },
            version: {
                message: `VERSION to install?`,
                required: true,
            }
        }
    }, invokeInstall);
}

function buildPath(path) {
    try {
        return fs.realpathSync(path);
    } catch (error) {
        console.error(`ERROR: Could not find ${path}`);
        return null;
    }
}

function removeAndInstall(pathToProject, lib, version, callback) {
    const resolvedPath = buildPath(`${__dirname}/${pathToProject}`);
    const removeDir = buildPath(`${resolvedPath}/node_modules/` + lib);
    if (removeDir !== null) {
        console.log(`remove dir in ${removeDir}`);
        deleteFolderRecursive(removeDir);
    }
    process.chdir(resolvedPath);
    console.log('In directory ' + process.cwd());
    install(lib + '@' + version, callback);
}

//remove folder Synchronously
function deleteFolderRecursive(path) {
    if (fs.existsSync(path)) {
        fs.readdirSync(path).forEach(function (file, index) {
            var curPath = path + "/" + file;
            if (fs.lstatSync(curPath).isDirectory()) { // recurse
                deleteFolderRecursive(curPath);
            } else { // delete file
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(path);
    }
}

function install(packages, callback) {
    console.log('installing ', packages);
    let saveSnippet = isDevDependency ? ' --save-dev -E' : ' --save -E';
    let command = 'npm prune && npm install && npm install ' + packages + saveSnippet;
    if (shouldRunMaven) {
        command += ' && mvn clean install -DskipTests';
    }
    const child = exec(command,
        function (error, stdout, stderr) {
            if (error !== null) {
                callback(error);
            }
            callback(error, stdout);
        });
}
