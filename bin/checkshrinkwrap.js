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
const PRECISE_VERSION_CHARS_PATTERN = /^\d+\.\d+\.\d+(-[A-Za-z0-9.]+)*$/;

const start = new Date().getTime();

checkProject('../blueocean-dashboard');
checkProject('../blueocean-personalization');
checkProject('../blueocean-web');
checkProject('../blueocean-config');
checkProject('../blueocean-core-js');
checkProject('../blueocean-pipeline-editor');
checkProject('../jenkins-design-language');

const elapsed = new Date().getTime() - start;
console.log(`all dependencies look good! took ${elapsed}ms`);
// done!

function checkProject(pathToProject) {
    const resolvedPath = buildPath(`${__dirname}/${pathToProject}`);
    console.log(`validating dependencies in ${resolvedPath}`);
    const packageJsonPath = buildPath(`${resolvedPath}/package.json`);
    try {
        fs.realpathSync(`${resolvedPath}/npm-shrinkwrap.json`);
    } catch (error) {
        return; // no shrinkwrap file, is ok for now
    }
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
    validateExplicitDependencies(shrinkwrap);
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
        console.error(`did you use 'npm install dep --save/-dev -E' ?`);
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
    if (!shrinkwrap.dependencies[name].from) {
        return;
    }
    if (shrinkwrap.dependencies[name].from.startsWith("..") || shrinkwrap.dependencies[name].resolved.startsWith("file:")) {
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

function validateExplicitDependencies(shrinkwrap) {
    const depRules = {
        'create-hmac': {
            version: '1.1.4',
            message: 'create-hmac cannot exceed 1.1.4 due to an incompatibility with safe-buffer and buffer. the version may have changed if you recently upgraded js-builder. discard the updates in npm-shrinkwrap.json manually. see https://github.com/crypto-browserify/createHmac/issues/20'
        },
    };

    function transformDeps(dependencies) {
        // transform an dependency object keyed by name to an array of child dependency objects with a 'name' property.
        return Object.keys(dependencies).map(depName => {
            const dependency = dependencies[depName];
            dependency.name = depName;
            return dependency;
        });
    }

    const depsToCheck = transformDeps(shrinkwrap.dependencies);
    const failedDeps = [];

    while (depsToCheck.length) {
        const currentDep = depsToCheck.shift();
        if (depRules[currentDep.name] && depRules[currentDep.name].version !== currentDep.version) {
            const rule = depRules[currentDep.name];
            failedDeps.push(`${currentDep.name}:${rule.version}: ${rule.message}`);
        }
        if (currentDep.dependencies) {
            depsToCheck.push(...transformDeps(currentDep.dependencies));
        }
    }

    if (failedDeps.length) {
        console.error('explicit version checks in shrinkwrap failed');
        failedDeps.forEach(message => console.error(message));
        process.exit(1);
    }
}
