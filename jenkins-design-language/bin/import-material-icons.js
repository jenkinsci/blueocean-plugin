#!/usr/bin/env node

const MESSAGE = ` 
******************************************************************************
**                                                                          **
**   Import SVG icons from a source checkout (not install) of material-ui   **
**   (see: https://github.com/callemall/material-ui ).                      **
**                                                                          **
**   Copies the files to JDL, while removing references to recompose/pure   **
**                                                                          **
**   USAGE: bin/import-material-icons.js /path/to/checkouts/material-ui     **
**                                                                          **
******************************************************************************
`;

const Bluebird = require('bluebird');
const fs = require('fs');
const Spinner = require('cli-spinner').Spinner;
const pathUtils = require('path'); // Renaming to avoid dumb errors :)

const jdlPackageJSON = require('../package.json');

Bluebird.promisifyAll(fs);

//--------------------------------------------------------------------------
//  Config

const sourcePackageName = 'material-ui-build';
const sourceIconSubdir = 'src/svg-icons'; // Subdir within checkout where the icons live
const relativeDestPath = './src/js/components/material-ui/svg-icons/';

//--------------------------------------------------------------------------
//  Main

function main() {

    const jdlPath = getJDLPath();
    const destPath = pathUtils.resolve(jdlPath, relativeDestPath);
    const sourceCheckoutPath = getSourceCheckoutPath();
    const sourceIconsRoot = sourceCheckoutPath && pathUtils.resolve(sourceCheckoutPath, sourceIconSubdir);

    if (!sourceCheckoutPath) {
        console.log(MESSAGE);
        return;
    }

    let spinner;

    validateSourcePath(sourceCheckoutPath)
        .then(() => {
            spinner = new Spinner('Searching');
            spinner.start();
        })
        .then(() => findSourceFiles(sourceIconsRoot))
        .then(iconFilePaths => iconFilePaths.map(filePath => {
            // Calculate how the absolute path relates back to the root of the icons dir
            const relativePath = pathUtils.relative(sourceIconsRoot, filePath);
            return { filePath, relativePath };
        }))
        .then(iconFiles => {
            spinner.setSpinnerTitle(`Processing ${iconFiles.length} source files`);
            return Promise.all(iconFiles.map(iconFile => processIcon(iconFile, destPath)));
        })
        .then(results => {
            // Copy index.js
            return fs.readFileAsync(pathUtils.resolve(sourceIconsRoot, 'index.js'), { encoding: 'UTF8' })
                .then(contents => fs.writeFileAsync(pathUtils.resolve(destPath, 'index.js'), contents, { encoding: 'UTF8' }))
                .then(() => results);
        })
        .then(results => {
            spinner.stop();

            const errors = results.filter(details => details.error);
            const successful = results.length - errors.length;

            if (successful > 0) {
                console.log('\x1b[32m');
                console.log(`${successful} Icons successfully copied and updated.`);
                console.log('\x1b[m');
            }

            if (errors.length) {
                process.exitCode = -1;
                console.log('\x1b[33m');
                console.error(`${errors.length} Icons had problems:`);
                for (const { relativePath, error } of errors) {
                    console.error(` * ${relativePath} - ${error}`);
                }
                console.error('\x1b[m');
            }
        })
        .catch(e => {
            spinner && spinner.stop();
            throw e;
        });
}

//--------------------------------------------------------------------------
//  Steps

function getJDLPath() {
    if (jdlPackageJSON.jdlName !== 'jenkins-design-language') {
        throw new Error('Script is not in /bin of JDL home');
    }
    return pathUtils.resolve(__dirname, '..');
}

function getSourceCheckoutPath() {
    if (process.argv.length === 3) {
        const inputPath = process.argv[2];
        return pathUtils.resolve(inputPath); // Make sure we've resolved any relative path
    }
    return false;
}

/**
 * Make sure the source path is correct in that it exists and appears to point to the root of the repo we want.
 */
function validateSourcePath(sourcePath) {
    const materialPackageJSONPath = pathUtils.resolve(sourcePath, 'package.json');

    return fs.readFileAsync(materialPackageJSONPath, { encoding: 'UTF8' })
        .then(materialPackageJSONString => {
            const package = JSON.parse(materialPackageJSONString);
            if (package.name !== sourcePackageName) {
                throw new Error(`Source path ${sourcePath} does not appear to be a clone of https://github.com/callemall/material-ui`);
            }
            return sourcePath;
        });
}

/**
 * Traverse the tree starting at sourcePath, and find all the .js files.
 *
 * Only goes 3 levels deep, will throw if it gives up due to tree depth.
 *
 * Kind of ugly, but better than pulling in some npm module with 45 transitive dependencies.  Please let me know if
 * there's a nicer way to do this! - JM
 */
function findSourceFiles(sourceIconsRoot) {
    let visitedDirectories = [];
    let allSourceFiles = [];

    function recurseDir(dir, depth) {
        // Don't get in any loops
        if (visitedDirectories.indexOf(dir) !== -1) {
            return;
        }

        if (depth > 3) {
            throw new Error('findSourceFiles() - directory tree too deep');
        }

        visitedDirectories.push(dir);

        return fs.readdirAsync(dir)
            .then(filenames => {
                // Turn the list of filenames into paths
                const paths = filenames
                    .filter(filename => filename[0] !== '.' && filename !== 'index.js') // Ignore index, hidden, '.' and '..'
                    .map(filename => pathUtils.resolve(dir, filename));

                // For each path...
                const statPromises = paths.map(filePath => {
                    return fs.statAsync(filePath) // ... get stats
                        .then(stats => ({ filePath, stats })); // ... and associate with path
                });

                // Turn the array of promises into a promise of arrays.
                return Promise.all(statPromises);
            })
            .then(filePathsAndStats => {
                // Pick out all the JS files in dir, while collecting the list of subdirectories

                let subDirPaths = [];

                for (const { filePath, stats } of filePathsAndStats) {
                    if (stats.isDirectory()) {
                        subDirPaths.push(filePath);
                    }
                    else if (stats.isFile() && pathUtils.extname(filePath).toLowerCase() === '.js') {
                        allSourceFiles.push(filePath);
                    }
                }

                if (subDirPaths.length === 0) {
                    return;
                }

                // Recurse
                return Promise.all(subDirPaths.map(path => recurseDir(path, depth + 1)));
            });
    }

    return recurseDir(sourceIconsRoot, 0).then(() => allSourceFiles);
}

/**
 * Load an icon source file, remove the references to recompose/pure, then write into the JDL
 *
 * Very simplistic transform, and won't write anything if the file's not recognised / transformed correctly.
 */
function processIcon({ filePath, relativePath }, destPathRoot) {

    // The patterns we're looking for. Only write the new file if all are met.
    const pureImportRegex = /^import pure from 'recompose\/pure';\s*\n/gm;
    const componentDeclRegex = /let (\w+) = \(props\) =>/;
    const pureWrapRegex = /^((\w+)) = pure\(\1\);\s*\n/gm;

    // We'll const the component declaration while we're at it
    const componentDeclReplacement = 'const $1 = (props) =>';

    const destPath = pathUtils.resolve(destPathRoot, relativePath);

    return fs.readFileAsync(filePath, { encoding: 'UTF8' })
        .then(sourceCode => {

            const pureImportMatch = sourceCode.match(pureImportRegex);
            const componentDeclMatch = sourceCode.match(componentDeclRegex);
            const pureWrapMatch = sourceCode.match(pureWrapRegex);

            if (!(pureImportMatch && componentDeclMatch && pureWrapMatch)) {
                // Could not make sense of this file
                return {
                    filePath,
                    relativePath,
                    error: 'Could not recognise the source',
                };
            }

            // Update the source
            const newSourceCode = sourceCode
                .replace(pureImportRegex, '')
                .replace(pureWrapRegex, '')
                .replace(componentDeclRegex, componentDeclReplacement);

            return fs.writeFileAsync(destPath, newSourceCode, { encoding: 'UTF8' })
                .then(() => ({
                    filePath,
                    relativePath,
                    success: true,
                    className: componentDeclMatch[1],
                }))
        });
}

// Make rocket go now
main();
