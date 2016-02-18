/**
 * Re-export things for convenience. 
 */

import {ExtensionPoint, extensionPointStoreSingleton} from './extension-point.jsx';
import {store, actions} from './stores';

export const extensionPointStore = extensionPointStoreSingleton; // TODO: remove ugly global
export {ExtensionPoint, store, actions};
