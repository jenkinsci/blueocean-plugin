#!/usr/bin/env node
/**
 * This script will install the package and version you want in the directories
 * that are defined in below array. It accept a full version string
 * e.g. @jenkins-cd/design-language@0.0.105-TimePrecise
 * as input and if you do not provide that it will start a prompt.
 *
 * We will prune and install BEFORE we install the requested version to make sure that
 * shrinkwrap will update correctly everytime. We further do a mvn install afterwards
 * to publish the new hpi to the local .m2 repository
 */
const fs = require('fs');
const async = require('async');
const exec = require('child_process').exec;
const prompt = require('prompt');

const start = new Date().getTime();
const directories = ['../blueocean-dashboard', '../blueocean-personalization', '../blueocean-web'];
function invokeInstall(err, result) {
    // Log the results.
    console.log('Command-line input received:');
    console.log('package: ' + result.package);
    console.log('version: ' + result.version);
    // const lib = '@jenkins-cd/design-language';
    // const version = '0.0.79-unpublishedthor1';
    async.mapSeries(directories, function (elem, callback) {
        console.log('Current element', elem);
        removeAndInstall(elem, result.package,  result.version, callback);
    }, function (err, result) {
        if (err) {
            console.error('Something went wrong', err);
        }
        const ellapsed = new Date().getTime() - start;
        console.log(`Install look good! took ${ellapsed}ms`);
        process.exit(0);
    });
}
if (process.argv[2]) {
  const versionArray = process.argv[2].split('@');
  const result = {};
  if (versionArray.length > 2) {
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
//remove folder Syncronously
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
    const child = exec('npm prune; npm install; npm install ' + packages + ' --save -E; mvn clean install -DskipTests',
        function (error, stdout, stderr) {
            if (error !== null) {
                callback(error);
            }
            callback(error, stdout);
        });
}
