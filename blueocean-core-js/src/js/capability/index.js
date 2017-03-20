/**
 * Created by cmeyers on 9/8/16.
 */
import { CapabilityApi } from './CapabilityApi';
import { CapabilityStore } from './CapabilityStore';
import { CapabilityAugmenter } from './CapabilityAugmenter';

const api = new CapabilityApi();
const store = new CapabilityStore(api);
const augmenter = new CapabilityAugmenter(store);

// export as named singletons
export { store as capabilityStore };
export { augmenter as capabilityAugmenter };
export { capable } from './Capable';
