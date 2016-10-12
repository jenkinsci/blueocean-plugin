#!/usr/bin/env node

const fs = require('fs');
const async = require('async');
const exec = require('child_process').exec;
const prompt = require('prompt');

const start = new Date().getTime();
const directories = ['../blueocean-dashboard', '../blueocean-personalization', '../blueocean-web'];

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
}, function (err, result) {
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
});

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
    const child = exec('npm install ' + packages + ' --save -E',
        function (error, stdout, stderr) {
            if (error !== null) {
                callback(error);
            }
            callback(error, stdout);
        });
}