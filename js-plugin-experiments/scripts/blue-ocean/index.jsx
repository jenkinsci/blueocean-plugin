/**
 * Re-export things for convenience. 
 */

import {ExtensionPoint, extensionPointStoreSingleton} from './extension-point.jsx';

export const extensionPointStore = extensionPointStoreSingleton; // TODO: remove ugly global
export {ExtensionPoint};
