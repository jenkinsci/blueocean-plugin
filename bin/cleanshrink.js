/*********************************************************************************************
 **********************************************************************************************

 Strips out the "resolved" property from npm-shrinkwrap.json so that registry setting from .npmrc is respected.
 Usage:
 node cleanshrink.js
 ** Excepts to find npm-shrinkwrap.json in same dir as cwd.

 **********************************************************************************************
 *********************************************************************************************/

var fs = require('fs');
var shrinkPath = process.cwd() + '/npm-shrinkwrap.json';

console.log('cleaning shrinkwrap at: ' + shrinkPath);

var shrinkwrap = require(shrinkPath);

function replacer(key, val) {
    if (key === 'resolved' && this.from && this.version) {
        return undefined;
    } else {
        return val;
    }
}

fs.writeFileSync(shrinkPath, JSON.stringify(shrinkwrap, replacer, 2));
