#! /usr/bin/env node
'use strict';

const child_process = require('child_process');
const os = require('os');
const fs = require('fs');

// --[ Helpers ]------------------------------------------------------------------------------

function execSync(command, ...args) {
    let result = child_process.spawnSync(command, args || [], { encoding: 'utf8' });

    // Docs say errors are thrown, but apparently not
    if (result.error) {
        throw new Error(JSON.stringify(result.error, null, 4));
    } else if (result.status !== 0) {
        throw new Error(result.stderr);
    }

    return result.stdout;
}

// --[ Main ]---------------------------------------------------------------------------------

try {
    // Get staged files from git
    const stagedFiles = execSync('git', 'diff', '--name-only', '--staged').split(os.EOL);

    // Prune out any whitespace lines or deleted files (which show up in --staged)
    const filteredFiles = stagedFiles.filter(filePath => filePath && fs.existsSync(filePath));

    if (filteredFiles.length === 0) {
        console.log('No staged files to format.');
        return;
    }

    // Reformat files
    const prettyResult = execSync('node', 'bin/pretty.js', '--fix-show-changed', ...filteredFiles);
    const formattedFiles = prettyResult.split(os.EOL).filter(filePath => !!filePath);

    // Re-stage formatted files
    const gitOutput = execSync('git', 'add', ...formattedFiles);
    console.log(gitOutput);
} catch (err) {
    console.error(err);
    process.exitCode = -1;
    return;
}
