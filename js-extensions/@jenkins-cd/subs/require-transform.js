var transformTools = require('browserify-transform-tools');

function getExported(name) {
    var importName = name.replace(/\@/,'').replace('/','-');
    var out = "require('@jenkins-cd/js-modules').requireModule('" + importName + ':' + importName + "@any')";
    return out;
}

var directModuleReplacements = {
    '@jenkins-cd/js-extensions': 0,
    '@jenkins-cd/blueocean-core-js': 0,
    '@jenkins-cd/design-language': 0,
    'react': 0,
    'react-dom': 0,
};

var replacementKeys = Object.keys(directModuleReplacements);
for (var i = 0; i < replacementKeys.length; i++) {
    var k = replacementKeys[i];
    directModuleReplacements[k] = getExported(k);
}

function getImports(packageJson) {
    if (!packageJson || !packageJson.jenkinscd || !packageJson.jenkinscd.import) {
        return {};
    }
    var out = {};
    packageJson.jenkinscd.import.map(function(imp) {
        var name = imp.replace(/\@any/,'');
        out[name] = getExported(name);
    });
    return out;
}

var makeTransform = function (file, exports, imports) {
    return transformTools.makeFunctionTransform('exclude-upstream-requires', {
        jsFilesOnly: true,
        global: true,
        functionNames: ['require']
    }, function (functionParams, opts, done) {
        var name = functionParams.args && functionParams.args.length && functionParams.args[0].value;
        if (exports.indexOf(name) !== -1) {
            return done();
        }
        var replacement = imports[name];
        if (replacement) {
            return done(null, replacement);
        }
        if (exports.indexOf('react') === -1 && name.indexOf('react/') !== -1) {
            console.error(" stray react library: ", name, 'in', opts.file);
        }
        return done();
    });
};

module.exports = function(packageJson) {
    var imports = Object.assign(getImports(packageJson), directModuleReplacements);
    return function (file, config) {
        var wrappedTransform = makeTransform(file, config.exports, imports);
        return wrappedTransform(file, config);
    }
};

module.exports.configure = function (config) {
    return function (file) {
        return module.exports(file, config);
    };
};

module.exports.makeTransform = makeTransform;
