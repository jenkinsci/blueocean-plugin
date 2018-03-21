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
const directories = ['../blueocean-dashboard', '../blueocean-personalization', '../blueocean-web', '../blueocean-pipeline-editor', '../blueocean-core-js', '../jenkins-design-language'];

var isDevDependency = false; // Set via command line
var shouldRunMaven = false; // Set via command line

function invokeInstall() {
    async.map(directories,
        function (elem, callback) {
            console.log('Current element', elem);
            removeAndInstall(elem, callback);
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


invokeInstall();


function buildPath(path) {
    try {
        return fs.realpathSync(path);
    } catch (error) {
        console.error(`ERROR: Could not find ${path}`);
        return null;
    }
}
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
function removeAndInstall(pathToProject, callback) {
    const resolvedPath = buildPath(`${__dirname}/${pathToProject}`);
    const removeDir = buildPath(`${resolvedPath}/node_modules/`);
    const shrinkwrap = `${resolvedPath}/npm-shrinkwrap.json`
    if (fs.existsSync(shrinkwrap)) {
        fs.unlinkSync(shrinkwrap);
    }

    deleteFolderRecursive(removeDir);
    
    process.chdir(resolvedPath);
    console.log('In directory ' + process.cwd());
    console.log('installing');
    let command = 'npm install && npm shrinkwrap --dev ';
    console.log('running ' + command);
    const child = exec(command,
        function (error, stdout, stderr) {
            console.error(`Error in ${resolvedPath}`);
            if (error !== null) {
                callback(error);
            }
            callback(error, stdout);
        });
}
