#! /usr/bin/env node
'use strict';

const prettier = require('prettier');
const path = require('path');
const glob = require('glob');
const fs = require('graceful-fs'); // Will backoff on EMFILE

// --[ Parse args ]---------------------------------------------------------------------------

function usage() {
    console.log('Formats or checks JS code via prettier.js');
    console.log();
    console.log('Usage:');
    console.log();
    console.log('node pretty.js filesGlob1 ... filesGlobN');
    console.log('    Check only, reports errors and exit(1)');
    console.log();
    console.log('node pretty.js --fix filesGlob1 ... filesGlobN');
    console.log('    Format files in place, human-friendly output');
    console.log();
    console.log('node pretty.js --fix-show-changed filesGlob1 ... filesGlobN');
    console.log('    Same as --fix, but on success outputs only a list of changed files');
}

let args = process.argv.slice(2); // drop "node" and "path/to/script"
let sourceGlobs = [];
let fixMode = false;
let isDebug = false;
let simplifyOutput = false;

while (args.length > 0) {
    let arg = args.shift();

    if (arg === '--debug') {
        isDebug = true;
    } else if (arg === '--fix') {
        fixMode = true;
    } else if (arg === '--fix-show-changed') {
        fixMode = true;
        simplifyOutput = true;
    } else {
        sourceGlobs.push(arg);
    }
}

if (sourceGlobs.length === 0) {
    usage();
    process.exitCode = 1;
    return;
}

// --[ Steps ]--------------------------------------------------------------------------------

function getConfig(projectBaseDir) {
    return prettier.resolveConfig(projectBaseDir);
}

function getSourceFilesFromGlob(globPattern, ignoreGlobs) {
    return new Promise((fulfil, reject) => {
        glob(globPattern, { ignore: ignoreGlobs }, (err, files) => {
            if (err) {
                reject(err);
            } else {
                fulfil(files);
            }
        });
    });
}

// Make sure we only use valid extensions, and each fileName appears only once
function filterFiles(files, validExtensions) {
    const accepted = [];

    for (const fileName of files) {
        if (accepted.indexOf(fileName) === -1 && validExtensions.indexOf(path.extname(fileName).toLowerCase()) !== -1) {
            accepted.push(fileName);
        }
    }

    return accepted;
}

function getSourceFilesForAllGlobs(config) {
    const ignoreGlobs = config.jenkins.ignoreGlobs;
    const validExtensions = config.jenkins.extensions;

    return Promise.all(sourceGlobs.map(sourceGlob => getSourceFilesFromGlob(sourceGlob, ignoreGlobs)))
        .then(filesArrays => Array.prototype.concat.apply([], filesArrays)) // Flatten
        .then(files => filterFiles(files, validExtensions))
        .then(files => ({ files, config }));
}

function loadSource(sourcePath) {
    return new Promise((fulfil, reject) => {
        fs.readFile(sourcePath, 'utf8', (err, data) => {
            if (err) {
                reject(err);
            } else {
                fulfil(data);
            }
        });
    });
}

function saveSource(sourcePath, data) {
    return new Promise((fulfil, reject) => {
        fs.writeFile(sourcePath, data, 'utf8', err => {
            if (err) {
                reject(err);
            } else {
                fulfil(true);
            }
        });
    });
}

function prettifyFiles(files, config) {
    let unformattedFiles = [];
    let formattedFiles = [];
    let errors = [];

    // Clean our custom prop from the config, so we don't annoy prettier with it
    const cleanedConfig = Object.assign({}, config);
    delete cleanedConfig.jenkins;

    let filePromises = files.map(sourcePath => {
        return loadSource(sourcePath)
            .then(source => {
                if (fixMode) {
                    const newSource = prettier.format(source, cleanedConfig);
                    if (newSource !== source) {
                        return saveSource(sourcePath, newSource).then(() => {
                            formattedFiles.push(sourcePath);
                        });
                    }
                } else {
                    if (!prettier.check(source, cleanedConfig)) {
                        unformattedFiles.push(sourcePath);
                    }
                }
            })
            .catch(err => {
                // We have to catch and collect errors for each file Promise, because otherwise Promise.all
                // with fail early on a single error
                console.error('\x1b[31m' + sourcePath + '\x1b[m');
                console.error(err);
                console.error();
                errors.push(err);
                return false;
            });
    });

    return Promise.all(filePromises).then(() => ({ files, formattedFiles, unformattedFiles, errors }));
}

// Display results to user
function showResults(files, formattedFiles, unformattedFiles, errors) {
    const formattedCount = formattedFiles.length;
    const unformattedCount = unformattedFiles.length;
    const errorCount = errors.length;
    const filesCount = files.length;
    const okCount = filesCount - formattedCount - unformattedCount - errorCount;

    if (okCount > 0 && !simplifyOutput) {
        console.log(okCount + ' of ' + filesCount + ' files already correct.');
    }

    if (formattedCount > 0) {
        if (!simplifyOutput) {
            console.log('Formatted ' + formattedCount + ' of ' + filesCount + ' files:');
        }
        for (const sourcePath of formattedFiles) {
            if (simplifyOutput) {
                console.log(sourcePath);
            } else {
                console.log('    - ' + sourcePath);
            }
        }
    }

    if (unformattedCount > 0) {
        // Should only happen when !fixMode
        console.error(unformattedCount + ' of ' + filesCount + ' files need formatting:');
        for (const sourcePath of unformattedFiles) {
            console.error('    - ' + sourcePath);
        }
        process.exitCode = 1;
    }

    if (errorCount > 0) {
        console.error('\x1b[32mErrors occured processing ' + errorCount + ' of ' + filesCount + ' files.\x1b[m');
        process.exitCode = -1;
    }

    return { formattedFiles, unformattedFiles };
}

function debugPoint(result) {
    if (isDebug) {
        console.log(
            '\x1b[33m\n--- DEBUG --- \n    ' +
                JSON.stringify(result, null, 4)
                    .split('\n')
                    .join('\n    ') +
                '\n--- /DEBUG --- \x1b[m\n'
        );
    }
    return result;
}

// --[ Main ]---------------------------------------------------------------------------------

const projectBaseDir = path.resolve(__dirname, '..');

getConfig(projectBaseDir)
    .then(debugPoint)
    .then(config => getSourceFilesForAllGlobs(config))
    .then(debugPoint)
    .then(({ files, config }) => prettifyFiles(files, config))
    .then(({ files, formattedFiles, unformattedFiles, errors }) => showResults(files, formattedFiles, unformattedFiles, errors))
    .then(debugPoint)
    .catch(err => {
        console.error(err);
        process.exitCode = -1;
    });
