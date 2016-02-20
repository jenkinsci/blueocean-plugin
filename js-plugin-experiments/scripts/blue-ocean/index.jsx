/**
 * Re-export things for convenience. 
 */

import extensions from '@jenkins-cd/js-extensions';
import {store, actions} from './stores';

export const extensionPointStore = extensions.store; // TODO: remove ugly global
export const ExtensionPoint = extensions.ExtensionPoint;
export {store, actions};
