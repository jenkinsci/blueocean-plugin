import * as fs from 'fs';
import * as YAML from 'yamljs';

/**
 * Different types of class metadata we need to know about
 */
export enum ExtensionPointMetadataType {
    ExtensionPoint,
    Extension,
    Injection,
    ExtensionList,
}

/**
 *
 */
export interface ExtensionPointMetadata {
    dataType: ExtensionPointMetadataType,
    file: string,
    type: string,
    component: string,
    extensionPoint: string,
    superClass?: string,
    componentLocation?: string,
}

/**
 * Writes the standard jenkins-js-extension.yaml file with all extensions and extension points
 */
export function writeJenkinsExtensionsYaml(dir: string, metadata: ExtensionPointMetadata[], immediateLoad: boolean = false) {
    console.log('writeJenkinsExtensionsYaml metadata:', metadata);

    function getFileName(e: ExtensionPointMetadata): string {
        return `./${e.file.replace(/\.[tj]sx?/,'').replace('src/main/ts/','')}`;
    }

    function getRequire(e: ExtensionPointMetadata): string {
        return `require('${getFileName(e)}');\n`;
    }

    const fileDependencies = {};

    let extensionOut = {
        extensions: <any[]>[],
        extensionPoints: <any[]>[],
        injections: <any[]>[],
        extensionLists: <any[]>[],
    };
    if (immediateLoad) {
        console.log('writing immediate load from:', new Error().stack);
        extensionOut.extensions.push({
            extensionPoint: 'jenkins.main.routes',
            component: './EmptyRoute',
        });

        fs.writeFileSync(dir + '/EmptyRoute.jsx',
            `export default {}`
        );
    }

    const extensions = metadata.filter(e => e.dataType === ExtensionPointMetadataType.Extension);
    const extensionPoints = metadata.filter(e => e.dataType === ExtensionPointMetadataType.ExtensionPoint);

    let requireNumber = 0;
    extensionPoints.forEach(e => {
        extensionOut.extensionPoints.push({
            extensionPoint: e.extensionPoint,
            generated: true,
        });
    });

    extensions.forEach(e => {
        const componentLocation = `Module${requireNumber++}`;
        // js extensions needs a jsx file
        fs.writeFileSync(`${dir}/${componentLocation}.jsx`,
            getRequire(e)
        );

        e.componentLocation = `./${componentLocation}`;
        extensionOut.extensions.push({
            extensionPoint: e.extensionPoint,
            component: e.componentLocation,
            componentType: e.component,
            generated: true,
        });
    });

    metadata.filter(e => e.dataType === ExtensionPointMetadataType.Injection).forEach(e => {
        extensionOut.injections.push({
            extensionPoint: e.extensionPoint,
            generated: true,
        });
    });

    metadata.filter(e => e.dataType === ExtensionPointMetadataType.ExtensionList).forEach(e => {
        extensionOut.extensionLists.push({
            extensionPoint: e.extensionPoint,
            generated: true,
        });
    });

    interface Consumer {
        (it: any): boolean;
    }

    // find not found!?
    const find = (arr: any[], finder: Consumer): any => {
        for (let i = 0; i < arr.length; i++) {
            const it = arr[i];
            if (finder(it)) {
                return it;
            }
        }
        return undefined;
    };

    extensionOut.extensions.sort((a: ExtensionPointMetadata, b: ExtensionPointMetadata) => {
        const e1 = find(extensions, it => it.componentLocation === a.component);
        const e2 = find(extensions, it => it.componentLocation === b.component);

        console.log('sorting', e1, e2);

        // check if a needs b
        if (e1.superClass === e2.component) {
            console.log('e1 after', e1.component, e2.component);
            return 1;
        }

        // check if b needs a
        if (e2.superClass === e1.component) {
            console.log('e2 after', e1.component, e2.component);
            return -1;
        }

        // revert to filename
        return e1.componentLocation.localeCompare(e2.componentLocation);
    });

    const yamlFileName = dir + '/jenkins-js-extension.yaml';
    let extensionMeta: {[key: string]: any[]} = {};
    try {
        extensionMeta = YAML.load(yamlFileName);
    } catch(e) {
        console.log(yamlFileName + ' not found');
    }
    const extensionSource: {[key: string]: any[]} = extensionOut;
    for (const k of Object.keys(extensionOut)) {
        if (!(k in extensionMeta)) {
            extensionMeta[k] = extensionSource[k];
        } else {
            // remove any generated entries
            extensionMeta[k] = extensionMeta[k].filter((e: any) => !e.generated);
            extensionMeta[k] = extensionMeta[k].concat(extensionSource[k]);
        }
    }
    const extensionYaml = YAML.stringify(extensionMeta);
    // and this needs some extension point defined
    fs.writeFileSync(yamlFileName, extensionYaml);
}
