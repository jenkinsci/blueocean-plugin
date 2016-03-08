//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('@jenkins-cd/js-builder');

// Disable js-builder based linting for now.
// Will get fixed with https://github.com/cloudbees/blueocean/pull/55
builder.lint('none');
