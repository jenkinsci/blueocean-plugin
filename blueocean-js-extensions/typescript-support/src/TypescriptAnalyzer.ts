/**
 * FIXME: implement this in a way compatible with watch()
 * https://github.com/Microsoft/TypeScript/wiki/Using-the-Compiler-API#incremental-build-support-using-the-language-services
 */

import * as ts from 'typescript';
import { ExtensionPointMetadata, writeJenkinsExtensionsYaml , ExtensionPointMetadataType } from './ExtensionPointMetadata';
import * as fs from 'fs';

function compile(fileNames: string[], options: ts.CompilerOptions) {
    let program = ts.createProgram(fileNames, options);
    let emitResult = program.emit();

    let allDiagnostics = ts.getPreEmitDiagnostics(program).concat(emitResult.diagnostics);

    allDiagnostics.forEach(diagnostic => {
        let { line, character } = diagnostic.file.getLineAndCharacterOfPosition(diagnostic.start);
        let message = ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n');
        console.log(`${diagnostic.file.fileName} (${line + 1},${character + 1}): ${message}`);
    });

    let exitCode = emitResult.emitSkipped ? 1 : 0;
    console.log(`Process exiting with code '${exitCode}'.`);
    process.exit(exitCode);
}

if('compile' in process.argv) {
    compile(process.argv.slice(2), {
        noEmitOnError: true,
        noImplicitAny: true,
        target: ts.ScriptTarget.ES5,
        module: ts.ModuleKind.CommonJS
    });
}

const debugLog = console.log;


/**
 * Get an identifier string from the node
 * @param node an identifier node, or other similar
 */
function getIdentifier(node: ts.Node): string {
    if (node.kind === ts.SyntaxKind.ExpressionStatement) {
        return getIdentifier((<ts.ExpressionStatement>node).expression);
    }
    if (node.kind === ts.SyntaxKind.Identifier) {
        return (<ts.Identifier>node).text;
    }
    if (node.kind === ts.SyntaxKind.Decorator) {
        return getIdentifier((<ts.Decorator>node).expression);
    }
    if (node.kind === ts.SyntaxKind.CallExpression) {
        return getIdentifier((<ts.CallExpression>node).expression);
    }
    if (node.kind === ts.SyntaxKind.ClassDeclaration) {
        return getIdentifier((<ts.ClassDeclaration>node).name);
    }
    if (node.kind === ts.SyntaxKind.ExpressionWithTypeArguments) {
        return getIdentifier((<ts.ExpressionWithTypeArguments>node).expression);
    }
    if (node.kind === ts.SyntaxKind.PropertyDeclaration) {
        return getIdentifier((<ts.PropertyDeclaration>node).name);
    }
    //case 'MemberExpression': {
    //    return getIdentifier(node.object) + '.' + getIdentifier(node.property);
    //}
    debugLog('Unhandled node identifier requested', node);
    throw new Error("Unexpected identifier type: " + node);
}

function getSuperClass(classDecl: ts.ClassDeclaration): string {
    if (classDecl.heritageClauses && classDecl.heritageClauses.length > 0) {
        return getIdentifier(classDecl.heritageClauses[0].types[0]);
    }
    return null; // no superclass
}

function findExtensionPointType(decorator: ts.Decorator, classDecl: ts.ClassDeclaration): string {
    // Is the extension point specified in the @Extension?
    if (decorator.expression.kind === ts.SyntaxKind.CallExpression) {
        const call = <ts.CallExpression>decorator.expression;
        if (call.arguments && call.arguments.length > 0) {
            return getIdentifier(call.arguments[0]);
        }
    }
    const superclass = getSuperClass(classDecl);
    if (!superclass) {
        throw new Error('Unable to determine extension point for: ' + getIdentifier(classDecl));
    }
    return superclass;
}

function findContainingClassDeclaration(node: ts.Node): ts.ClassDeclaration {
    if (node.kind === ts.SyntaxKind.ClassDeclaration) {
        return <ts.ClassDeclaration>node;
    }
    if (node.parent) {
        return findContainingClassDeclaration(node.parent);
    }
    return null;
}

function findExtensions(node: ts.Node, extensions: ExtensionPointMetadata[]) {
    switch(node.kind) {
        case ts.SyntaxKind.PropertyDeclaration: {
            if (node.decorators && node.decorators.length > 0) {
                node.decorators.forEach(d => {
                    const classDecl = findContainingClassDeclaration(node);
                    const className = getIdentifier(classDecl);
                    const decoratorName = getIdentifier(d);
                    if (decoratorName === 'Inject') {
                        debugLog('Got Inject: ', className); //d);
                        const prop = <ts.PropertyDeclaration>node;
                        extensions.push({
                            dataType: ExtensionPointMetadataType.Injection,
                            component: getIdentifier(prop),
                            extensionPoint: decoratorName,
                            file: node.getSourceFile().fileName,
                            type: className,
                        });
                    }
                    if (decoratorName === 'ExtensionList') {
                        const prop = <ts.PropertyDeclaration>node;
                        debugLog('Got ExtensionList: ', className); //d);
                        extensions.push({
                            dataType: ExtensionPointMetadataType.ExtensionList,
                            component: getIdentifier(prop),
                            extensionPoint: decoratorName,
                            file: node.getSourceFile().fileName,
                            type: decoratorName,
                        });
                    }
                });
            }
            break;
        }
        case ts.SyntaxKind.ClassDeclaration: {
            if (node.decorators && node.decorators.length > 0) {
                const classDecl = <ts.ClassDeclaration>node;
                const className = getIdentifier(classDecl.name);
                node.decorators.forEach(d => {
                    const decoratorName = getIdentifier(d);
                    if (decoratorName === 'Extension') {
                        debugLog('Got Extension: ', className); //d);
                        extensions.push({
                            dataType: ExtensionPointMetadataType.Extension,
                            component: className,
                            extensionPoint: findExtensionPointType(d, classDecl),
                            file: node.getSourceFile().fileName,
                            type: className,
                            superClass: getSuperClass(classDecl),
                        });
                    }
                    else if (decoratorName === 'ExtensionPoint') {
                        debugLog('Got ExtensionPoint: ', className); //d);
                        extensions.push({
                            dataType: ExtensionPointMetadataType.ExtensionPoint,
                            component: className,
                            extensionPoint: className,
                            file: node.getSourceFile().fileName,
                            type: className,
                        });
                    }
                });
            }
        }
    }

    // Process whole source
    ts.forEachChild(node, n => findExtensions(n, extensions));
}

interface StringConsumer {
    (string: string): void;
}

function findSource(dir: string): string[] {
    const files: string[] = [];
    recurse(dir, f => {
        if (/.*\.(ts|tsx)$/.test(f)) {
            files.push(f);
        }
    });
    return files;
}

function recurse(dir: string, acceptor: StringConsumer): void {
    fs.readdirSync(dir).forEach((file: string) => {
        const f = dir + '/' + file;
        const stat: fs.Stats = fs.statSync(f);
        if (stat && stat.isDirectory()) {
            recurse(f, acceptor);
        } else {
            acceptor(f);
        }
    });
}

export function exportMetadata(srcDir: string, outDir: string) {
    const fileNames = findSource(srcDir);
    const extensions: ExtensionPointMetadata[] = [];
    fileNames.forEach(fileName => {
        let sourceFile = ts.createSourceFile(fileName, fs.readFileSync(fileName).toString(), ts.ScriptTarget.ES6, /*setParentNodes */ true);
        findExtensions(sourceFile, extensions);
    });
    writeJenkinsExtensionsYaml(outDir, extensions);
}
