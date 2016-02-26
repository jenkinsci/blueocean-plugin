//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('jenkins-js-builder');

builder.bundle('src/main/js/blueocean-admin.js')
  .inDir('target/classes/io/jenkins/blueocean');
