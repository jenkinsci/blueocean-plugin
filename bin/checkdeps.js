"use strict";

/*********************************************************************************************
**********************************************************************************************

    Checks for version inconsistencies in PROD dependencies for top-level
    projects, and a few first-party "blessed" deps like JDL etc.

    Usage:

        node checkdeps.js

    Any conflicting PROD dependencies will be printed on STDERR, and it will exit(1)

    Any conflicting DEV or PEER dependencies for packages in @jenkins-cd/ npm group will be printed on STDERR,
    and it will exit(1)

    If no conflicts, or only non-jenkins PEER/DEV conflicts, normal exit(0)

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

packageFiles.push(require("../blueocean-dashboard/package.json"));
packageFiles.push(require("../blueocean-web/package.json"));
packageFiles.push(require("../blueocean-personalization/package.json"));
packageFiles.push(require("../blueocean-config/package.json"));
packageFiles.push(require("../blueocean-pipeline-editor/package.json"));
packageFiles.push(require("../js-extensions/package.json"));
packageFiles.push(require("../blueocean-core-js/package.json"));
packageFiles.push(require("../jenkins-design-language/package.json"));

packageFiles.forEach(packageFile => {

    addDependencies("prod", packageFile.dependencies, true);
    addDependencies("dev", packageFile.devDependencies, false);
    addDependencies("peer", packageFile.peerDependencies, false);

    function addDependencies(kind, deps, includeNonJenkins) {

        if (deps) {
            Object.keys(deps).forEach(dependency => {
                if (includeNonJenkins || dependency.startsWith("@jenkins-cd")) {
                    const version = deps[dependency];
                    initEntry(dependency, version);
                    allDependencies[dependency][version].push(packageFile.name + " (" + kind + ")");
                }
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
    console.log('You can use bin/cleanInstall to install the dominant dependency in various places.');
    process.exitCode = 1;
}
