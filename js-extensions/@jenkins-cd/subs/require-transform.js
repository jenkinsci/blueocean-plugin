var path = require('path');
var transformTools = require('browserify-transform-tools');

var directModuleReplacements = {
	'@jenkins-cd/logging': 'jenkins-cd-logging:jenkins-cd-logging@any',
	'@jenkins-cd/js-extensions': 'jenkins-cd-js-extensions:jenkins-cd-js-extensions@any',
	'@jenkins-cd/blueocean-core-js': 'jenkins-cd-blueocean-core-js:jenkins-cd-blueocean-core-js@any',
	'mobx': 'mobx:mobx@any',
	'mobx-react': 'mobx-react:mobx-react@any',
	'react': 'react:react@any',
	'react-dom': 'react-dom:react-dom@any',
	'react-router': 'react-router:react-router@any',
	'react-addons-css-transition-group': 'react-addons-css-transition-group:react-addons-css-transition-group@any',
};

var makeTransform = function(file, exports) {
  return transformTools.makeFunctionTransform('exclude-upstream-requires', {
    jsFilesOnly: true,
    global: true,
    functionNames: ['require']
  }, function(functionParams, opts, done) {
	  var name = functionParams.args && functionParams.args.length && functionParams.args[0].value;
	  if (exports.indexOf(name) !== -1) {
		  return done();
	  }
	  var replacement = directModuleReplacements[name];
      if (replacement) {
    	  result = "require('@jenkins-cd/js-modules').requireModule('" + replacement + "')";
    	  return done(null, result);
      }
      if (exports.indexOf('react') === -1 && name.indexOf('react/') !== -1) {
    	  console.error(" stray react library: ", name, 'in', opts.file);
      }
	  return done();
  });
};

module.exports = function(file, config) {
  var wrappedTransform = makeTransform(file, config.exports);
  return wrappedTransform(file, config);
};

module.exports.configure = function(config) {
  return function(file) {
    return module.exports(file, config);
  };
};

module.exports.makeTransform = makeTransform;