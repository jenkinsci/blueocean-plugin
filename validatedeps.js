#!/usr/bin/env node
/**
 * Created by cmeyers on 9/20/16.
 */
const fs = require('fs');
const start = new Date().getTime();

const args = process.argv.slice(2);

var projectPath = '';

if (args.length === 1) {
    const pathParts = args[0].split('=');

    if (pathParts.length !== 2) {
        console.error('you must specify a project path in the form -p=path/to/project');
        process.exit(1);
    }

    projectPath = pathParts.slice(-1);
}

const workingPath = process.cwd();
const resolvedPath = buildPath(`${workingPath}/${projectPath}`);
console.log(`validating dependencies in ${resolvedPath}`);
const packageJsonPath = buildPath(`${resolvedPath}/package.json`);
const shrinkwrapJsonPath = buildPath(`${resolvedPath}/npm-shrinkwrap.json`);

// match x, tilde, gt, lt, star, carat or whitespace in version
const IMPRECISE_VERSION_CHARS_PATTERN = /[x~><*|\^\s]+/;

const packages = require(packageJsonPath);
const packageDeps = packages.dependencies;
const packageDevDeps = packages.devDependencies;

checkImpreciseDependencies(packageDeps);
checkImpreciseDependencies(packageDevDeps);
checkDuplicateDependencies(packageDeps, packageDevDeps);

const allDeps = Object.assign({}, packageDeps, packageDevDeps);
const shrinkwrap = require(shrinkwrapJsonPath);
validateDepsAgainstShrinkwrap(allDeps, shrinkwrap);

const ellapsed = new Date().getTime() - start;
console.log(`dependencies look good! took ${ellapsed}ms`);
// done!



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

        if (IMPRECISE_VERSION_CHARS_PATTERN.test(version)) {
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
        process.exit(1);
    }
}
