#!/usr/bin/env node

/**
 * TypeScript extension builder
 */

var tse = require('./target/TypescriptAnalyzer');
var srcDir = process.argv.length > 2 ? process.argv[2] : process.cwd();
var outDir = process.argv.length > 3 ? process.argv[3] : (srcDir + '/../target');
tse.exportMetadata(srcDir, outDir);
