import * as extensionAnalyzerPlugin from './ExtensionAnalyzerPlugin';

var syntaxDecorators = require("babel-plugin-syntax-decorators");
var syntaxFlow = require("babel-plugin-syntax-flow");
var transformDecoratorsLegacy = require("babel-plugin-transform-decorators-legacy").default;
var transformClassProperties = require("babel-plugin-transform-class-properties");

module.exports = {
    plugins: [
        syntaxDecorators,
        syntaxFlow,
        extensionAnalyzerPlugin,
        transformDecoratorsLegacy, // must come before transformClassProperties
        transformClassProperties,
    ]
};
