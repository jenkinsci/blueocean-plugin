"use strict";

/*********************************************************************************************
**********************************************************************************************

    Checks for version inconsistencies in top-level project dependencies.

    Usage:

        node checkdeps.js

    If conflicting versions are detected, this will print them out as JSON on STDERR and
    exit(1). If no conflicts are detected, there is no output and normal exit

**********************************************************************************************
*********************************************************************************************/

// --[ State ]--------------------------------------------------------------------------------

var allDependencies = {};

// --[ Helpers ]------------------------------------------------------------------------------

function initEntry(dependency, version) {
    if (!allDependencies.hasOwnProperty(dependency)) {
        allDependencies[dependency] = {};
    }

    var versions = allDependencies[dependency];

    if (!versions.hasOwnProperty(version)) {
        versions[version] = [];
    }
}

// --[ Consume package.json files ]-----------------------------------------------------------

var packageFiles = [];

packageFiles.push(require("./blueocean-dashboard/package.json"));
packageFiles.push(require("./blueocean-web/package.json"));

packageFiles.forEach(packageFile => {

    addDependencies("prod", packageFile.dependencies);
    addDependencies("dev", packageFile.devDependencies);
    addDependencies("peer", packageFile.peerDependencies);

    function addDependencies(kind, deps) {
        if (deps) {
            Object.keys(deps).forEach(dependency => {
                const version = deps[dependency];
                initEntry(dependency, version);
                allDependencies[dependency][version].push(packageFile.name + " (" + kind + ")");
            });
        }
    }
});

// --[ Check + print results ]----------------------------------------------------------------

let errs = [];

Object.keys(allDependencies).forEach(dependency => {
    let versions = allDependencies[dependency];

    if (Object.keys(versions).length !== 1) {
        let err = {};
        err[dependency] = versions;
        errs.push(err);
    }
});

if (errs.length) {
    console.error(JSON.stringify(errs, null, 4));
    process.exit(1);
}
