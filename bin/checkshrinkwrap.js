#!/usr/bin/env node

/*********************************************************************************************
 **********************************************************************************************

 Checks for imprecise version numbers in package.json, and compares deps/devDeps between package/shrinkwrap jsons.
 Usage:

 node checkshrinkwrap.js

 - Any imprecise version number (e.g. ~, ^, >=, etc) in package.json "dependencies" or "devDependencies" will fail
 - Any dependency in package.json but not in npm-shrinkwrap.json will fail
 - Any version mismatch in the above will fail

 All failures will exit(1). Otherwise, normal exit(0)

 **********************************************************************************************
 *********************************************************************************************/

const fs = require('fs');
// match "1.2.3" or "1.2.3-beta5"
const PRECISE_VERSION_CHARS_PATTERN = /^\d+\.\d+\.\d+(-[A-Za-z0-9]+)*$/;

const start = new Date().getTime();

checkProject('../blueocean-dashboard');
checkProject('../blueocean-personalization');
checkProject('../blueocean-web');
checkProject('../blueocean-config');
checkProject('../blueocean-core-js');

const ellapsed = new Date().getTime() - start;
console.log(`all dependencies look good! took ${ellapsed}ms`);
// done!

function checkProject(pathToProject) {
    const resolvedPath = buildPath(`${__dirname}/${pathToProject}`);
    console.log(`validating dependencies in ${resolvedPath}`);
    const packageJsonPath = buildPath(`${resolvedPath}/package.json`);
    const shrinkwrapJsonPath = buildPath(`${resolvedPath}/npm-shrinkwrap.json`);

    const packages = require(packageJsonPath);    
    const packageDeps = packages.dependencies;
    const packageDevDeps = packages.devDependencies;

    checkImpreciseDependencies(packageDeps);
    checkImpreciseDependencies(packageDevDeps);
    checkDuplicateDependencies(packageDeps, packageDevDeps);

    const allDeps = Object.assign({}, packageDeps, packageDevDeps);
    const shrinkwrap = require(shrinkwrapJsonPath);
    validateDepsAgainstShrinkwrap(allDeps, shrinkwrap);
    validateShrinkwrapResolve(shrinkwrap);
    console.log('success!');
}

function buildPath(path) {
    try {
        return fs.realpathSync(path);
    } catch (error) {
        console.error(`ERROR: Could not find ${path}`);
        process.exit(1);
    }
}

function checkImpreciseDependencies(dependencies) {
    const badDeps = [];    
    Object.keys(dependencies).forEach(name => {
        const version = dependencies[name];
        
        if (!PRECISE_VERSION_CHARS_PATTERN.test(version)) {
            badDeps.push(`${name}@${version}`);
        }
    });

    if (badDeps.length) {
        badDeps.forEach(dep => console.error(`${dep} must use precise version`));
        console.error(`did you use 'npm install dep --save/-dev -E' ?`)
        process.exit(1);
    }
}

function checkDuplicateDependencies(depList1, depList2) {
    const keys1 = Object.keys(depList1);
    const keys2 = Object.keys(depList2);
    const duplicates = keys1.concat(keys2).filter((name, index, allKeys) => index !== allKeys.indexOf(name));

    if (duplicates.length) {
        duplicates.forEach(name => console.error(`${name} is already defined in 'dependencies'; remove from 'devDependencies'`));
        process.exit(1);
    }
}

function validateShrinkwrapResolve(shrinkwrap) {
  
  Object.keys(shrinkwrap.dependencies).forEach(name => {
    if (shrinkwrap.dependencies[name].from.startsWith("..")) {
        console.error(`Bad shrinkwrap resolution: 'from' or 'resolved' refer to a project relative path not absolute URI from:${shrinkwrap.dependencies[name].from} resolved:${shrinkwrap.dependencies[name].resolved} in ${name}`);
        process.exit(1);
    }
  });
}

function validateDepsAgainstShrinkwrap(allDeps, shrinkwrap) {
    const badDeps = [];
    const shrinkDeps = shrinkwrap.dependencies;

    Object.keys(allDeps).forEach(name => {
        const version = allDeps[name];

        if (!shrinkDeps[name]) {
            badDeps.push(`${name}@${version} missing in shrinkwrap`);
        } else if (shrinkDeps[name].version !== version) {
            badDeps.push(`${name} should be ${version} but found ${shrinkDeps[name].version}`);
        }
    });

    if (badDeps.length) {
        badDeps.forEach(message => console.error(message));
        console.log('You can use bin/cleanInstall to install the dominant dependency in various places.');
        process.exit(1);
    }
}
