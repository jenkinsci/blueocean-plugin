/**
 * Babel js extensions plugin, processes Jenkins decorators
 */

import { ExtensionPointMetadata, writeJenkinsExtensionsYaml , ExtensionPointMetadataType } from 'blueocean-js-extensions-typescript-support';
import * as fs from 'fs';

// TODO
// auto-require react:
// https://github.com/mikaelbr/babel-plugin-transform-react-require/blob/master/src/index.js

var jenkinsModule = {
    exports: [],
    extensionPoints: [],
    extensions: [],
    injections: [],
};

var currentModule = [ JSON.parse(fs.readFileSync('package.json').toString()).name ];
var currentFile = [];
var currentFileState = [];
var currentClass = [];
var currentInjections = [];

// Write DI++ metadata on completion
// See: http://stackoverflow.com/questions/14031763/doing-a-cleanup-action-just-before-node-js-exits
function exitHandler(options, err) {
    if (err) {
        console.error(err);
    }
    else if (options.finalize) {
        //fs.writeFileSync('jenkinsMetadata.js', JSON.stringify(jenkinsModule));
        writeJenkinsExtensionsYaml('./target',
            jenkinsModule.injections.map(e => {
                return {
                    dataType: ExtensionPointMetadataType.Injection,
                    file: e.file,
                    type: e.type,
                    component: e.identifier,
                    extensionPoint: e.supertype,
                };
            }).concat(
            jenkinsModule.extensionPoints.map(e => {
                return {
                    dataType: ExtensionPointMetadataType.ExtensionPoint,
                    file: e.file,
                    type: e.type,
                    component: e.identifier,
                    extensionPoint: e.supertype,
                };
            })).concat(
            jenkinsModule.extensions.map(e => {
                return {
                    dataType: ExtensionPointMetadataType.Extension,
                    file: e.file,
                    type: e.type,
                    component: e.identifier,
                    extensionPoint: e.supertype,
                };
            })).concat(
            jenkinsModule.exports.map(e => {
                return {
                    dataType: ExtensionPointMetadataType.ExtensionList,
                    file: e.file,
                    type: e.type,
                    component: e.identifier,
                    extensionPoint: e.supertype,
                };
            }))
        );
        console.log(jenkinsModule);
    }
    if (options.exit) process.exit();
}
process.on('exit', function () {
    var args = arguments;
    exitHandler.call(null, {finalize: true, exit: true});
});
process.on('SIGINT', exitHandler.bind(null, {exit: true}));
process.on('uncaughtException', exitHandler.bind(null, {exit: true}));

/**
 * Get an identifier string from the node
 * @param node an identifier node, or other similar
 */
function getIdentifier(node) {
    switch (node.type) {
        case 'Identifier': {
            return node.name;
        }
        case 'MemberExpression': {
            return getIdentifier(node.object) + '.' + getIdentifier(node.property);
        }
        default: {
            throw new Error("Unexpected identifier type: " + node.type);
        }
    }
}

/**
 * Gets the identifier from a superclass expression
 */
function getSuperclassIdentifier(superclass) {
    switch(superclass.type) {
        case 'Identifier': {
            return getIdentifier(superclass);
        }
        case 'MemberExpression': {
            return getIdentifier(superclass);
        }
        default: {
            throw new Error('Unexpected super type for @extension' + superclass.type);
        }
    }
}

/**
 * Remove an item from an array, returning an new array
 */
function removeItem(a, i) {
    var d = [].concat(a.slice(0,i));
    d = d.concat(a.slice(i+1));
    return d;
}

function getCurrentFile() {
    return currentFile[currentFile.length-1];
}

function getCurrentModule() {
    return currentModule[currentModule.length-1];
}

function addProgramStatement(path, statement) {
    // See: http://stackoverflow.com/questions/35925798/how-to-add-an-import-to-the-file-with-babel
    path.unshiftContainer('body', [ statement ]);
}

module.exports = function (babel) {
    var t = babel.types;
    // uncomment the following line to get a list of all the node types:
    // console.log(t);
    return {
        //inherits: require("babel-plugin-transform-class-properties"),
        visitor: {
            Program: {
                enter(path, state) {
                    currentFile.push(state.file.opts.filename);
                    currentFileState.push(path);
                },
                exit(path) {
                    // Add necessary imports
                    var needsRegistry = false;
                    var anyInjections = false;
                    var imported = {};
                    for (var i = 0; i < jenkinsModule.extensions.length; i++) {
                        var extension = jenkinsModule.extensions[i];
                        if (extension.file === getCurrentFile()) {
                            if (imported[extension.identifier]) continue;
                            imported[extension.identifier] = true;
                            /*
                            addProgramStatement(path,
                                requireExternalClass({
                                    LOCAL_COMPONENT_REF: t.identifier(extension.extensionPointLocalId),
                                    EXTERNAL_CLASS_IDENTIFIER: t.stringLiteral(extension.supertype),
                                })
                            );
                            */
                            anyInjections = true;
                            needsRegistry = true;
                        }
                    }
                    for (var i = 0; i < jenkinsModule.extensionPoints.length; i++) {
                        var extensionPoint = jenkinsModule.extensionPoints[i];
                        if (extensionPoint.file === getCurrentFile()) {
                            needsRegistry = true;
                        }
                    }
                    for (var i = 0; i < jenkinsModule.exports.length; i++) {
                        var exported = jenkinsModule.exports[i];
                        if (exported.file === getCurrentFile()) {
                            needsRegistry = true;
                        }
                    }
                    for (var i = 0; i < jenkinsModule.injections.length; i++) {
                        var injection = jenkinsModule.injections[i];
                        if (injection.file === getCurrentFile()) {
                            if (imported[extension.identifier]) continue;
                            imported[extension.identifier] = true;

                            /*
                            addProgramStatement(path,
                                requireExternalClass({
                                    LOCAL_COMPONENT_REF: t.identifier(injection.injection),
                                    EXTERNAL_CLASS_IDENTIFIER: t.stringLiteral(injection.injection),
                                })
                            );
                            */
                            anyInjections = true;
                            needsRegistry = true;
                        }
                    }

                    if (anyInjections || needsRegistry) {
                        /*
                        addProgramStatement(path,
                            getPropertyFromExtensionRegistry()
                        );
                        addProgramStatement(path,
                            requireExtensionRegistry()
                        );
                        */
                    }
                    currentFile.pop();
                    currentFileState.pop();
                }
            },
            ClassDeclaration: {
                enter(path) {
                    currentClass.push(getIdentifier(path.node.id));
                    if (path.node.decorators) {
                        for (var i = 0; i < path.node.decorators.length; i++) {
                            var decorator = path.node.decorators[i];
                            if (decorator && decorator.expression) {
                                var target = null;
                                switch(decorator.expression.name) {
                                    case 'Export': {
                                        target = jenkinsModule.exports;
                                        break;
                                    }
                                    case 'ExtensionPoint': {
                                        target = jenkinsModule.extensionPoints;
                                        break;
                                    }
                                    case 'Extension': {
                                        target = jenkinsModule.extensions;
                                        break;
                                    }
                                }
                                if (target) {
                                    // TODO identifier will consist of a namespaced
                                    // dependent module and possibly file
                                    //var identifier = getCurrentModule() + '/' + getCurrentFile() + '/' + getIdentifier(path.node.id);
                                    var identifier = getIdentifier(path.node.id);
                                    var extensionType = null;
                                    var extensionPointLocalId = null;
                                    if (path.node.superClass) {
                                        extensionType = getSuperclassIdentifier(path.node.superClass);
                                        extensionPointLocalId = getIdentifier(currentFileState[currentFileState.length-1].scope.generateUidIdentifier(extensionType));
                                    }
                                    if (path.node.superClass) {
                                        path.node.superClass.name = extensionPointLocalId;
                                    }
                                    target.push({
                                        type: getIdentifier(path.node.id),
                                        file: getCurrentFile(),
                                        identifier: identifier,
                                        module: getCurrentModule(),
                                        supertype: extensionType,
                                        isBound: path.scope.hasBinding(identifier),
                                        extensionPointLocalId: extensionPointLocalId
                                    });
                                    // Add this type to the registry
                                    var insertionPoint = path;
                                    if (path.parentPath.type === 'ExportDefaultDeclaration'
                                    || path.parentPath.type === 'ExportNamedDeclaration') {
                                        insertionPoint = path.parentPath;
                                    }
                                    /*
                                    insertionPoint.insertAfter(registerWithExtensionRegistry({
                                        CLASS_NAME: t.identifier(getIdentifier(path.node.id)),
                                        IDENTIFIER: t.stringLiteral(identifier),
                                        EXTENSION_POINT: extensionType,
                                    }));
                                    // Remove the decorator
                                    path.node.decorators = removeItem(path.node.decorators, i);
                                    i--;
                                    */
                                }
                            }
                        }
                    }
                },
                exit() {
                    currentClass.pop();
                }
            },
            Class(path) {
                // must identify injected properties here, as they will
                // be removed by babel-plugin-transform-class-properties
                // taken from https://github.com/babel/babel/blob/master/packages/babel-plugin-transform-class-properties/src/index.js
                let isDerived = !!path.node.superClass;
                let constructor;
                let props = [];
                let body = path.get("body");

                for (let path of body.get("body")) {
                    if (path.isClassProperty()) {
                        props.push(path);
                    } else if (path.isClassMethod({ kind: "constructor" })) {
                        constructor = path;
                    }
                }

                if (!props.length) return;

                let nodes = [];
                let ref;

                if (path.isClassExpression() || !path.node.id) {
                    //nameFunction(path);
                    ref = path.scope.generateUidIdentifier("class");
                } else { // path.isClassDeclaration() && path.node.id
                    ref = path.node.id;
                }

                const currentType = getIdentifier(ref);

                //const instanceBody = [];

                // Add this type to the registry
                let insertionPoint = path;
                if (path.parentPath.type === 'ExportDefaultDeclaration'
                || path.parentPath.type === 'ExportNamedDeclaration') {
                    insertionPoint = path.parentPath;
                }

                for (const prop of props) {
                    const propNode = prop.node;
                    if (!propNode.decorators || propNode.decorators.length === 0) continue;
                    for (var i = 0; i < propNode.decorators.length; i++) {
                        var decorator = propNode.decorators[i].expression;
                        if (decorator.name === 'inject') {
                            currentInjections.push(getIdentifier(propNode.key));
                            //path.node.decorators = removeItem(path.node.decorators, i);
                            //i--;
                            var annotation = propNode.typeAnnotation.typeAnnotation;
                            var property = propNode;
                            if (property.type === 'ClassProperty') {
                                switch(annotation.type) {
                                    case 'GenericTypeAnnotation': {
                                        if (annotation.typeParameters) {

                                        }
                                        jenkinsModule.injections.push({
                                            type: currentClass[currentClass.length-1],
                                            name: getIdentifier(property.key),
                                            file: getCurrentFile(),
                                            injection: getIdentifier(annotation.id)
                                        });

                                        currentInjections.pop();
                                        currentInjections.push({
                                            injection: getIdentifier(annotation.id)
                                        });
                                        break;
                                    }
                                    case 'ArrayTypeAnnotation': {
                                        jenkinsModule.injections.push({
                                            type: currentClass[currentClass.length-1],
                                            name: getIdentifier(property.key),
                                            file: getCurrentFile(),
                                            injection: getIdentifier(annotation.elementType.id),
                                            injectAll: true,
                                        });

                                        currentInjections.pop();
                                        currentInjections.push({
                                            injection: getIdentifier(annotation.elementType.id)
                                        })
                                        break;
                                    }
                                    default: {
                                        throw new Error('Unexpected type annotation for @inject: ' + annotation.type);
                                    }
                                }
                            }
                        }
                    }
                }

                jenkinsModule.injections.forEach(i => {
                    if (currentType === i.type) {
                        /*
                        insertionPoint.insertAfter(registerFieldInjectionWithExtensionRegistry({
                            CLASS_IDENTIFIER: t.identifier(i.type),
                            PROPERTY_NAME: t.stringLiteral(i.name),
                            INJECTION_IDENTIFIER: t.stringLiteral(i.injection),
                            INJECT_ALL: t.booleanLiteral(i.injectAll ? true : false),
                        }));
                        */
                    }
                });
            },
            ClassProperty: {
                enter(path) {
                    // handle injections
                    if (path.node.decorators) {
                        for (var i = 0; i < path.node.decorators.length; i++) {
                            var decorator = path.node.decorators[i].expression;
                            if (decorator.name === 'inject') {
                                currentInjections.push(getIdentifier(path.node.key));
                                //path.node.decorators = removeItem(path.node.decorators, i);
                                //i--;
                            }
                        }
                    }
                },
                exit(path) {
                    var replacement = currentInjections.pop();
                    if (replacement) {
                        //path.insertBefore(
                        //    getPropertyFromExtensionRegistry(t, {
                        //        PROPERTY_NAME: t.identifier(replacement.injection),
                        //        EXTERNAL_CLASS_IDENTIFIER: t.stringLiteral(replacement.injection),
                        //    })
                        //);
                        //path.remove();
                    }
                }
            },
            TypeAnnotation(path) {
                // not currently handling an injection annotation
                if (currentInjections.length === 0) {
                    return;
                }
                var annotation = path.node.typeAnnotation;
                var property = path.parentPath.node;
                if (property.type === 'ClassProperty') {
                    switch(annotation.type) {
                        case 'GenericTypeAnnotation': {
                            if (annotation.typeParameters) {

                            }
                            jenkinsModule.injections.push({
                                type: currentClass[currentClass.length-1],
                                name: getIdentifier(property.key),
                                injection: getIdentifier(annotation.id)
                            });

                            currentInjections.pop();
                            currentInjections.push({
                                injection: getIdentifier(annotation.id)
                            })
                            break;
                        }
                        default: {
                            throw new Error('Unexpected type annotation for @inject: ' + annotation.type);
                        }
                    }
                }
            }
        }
    };
};
