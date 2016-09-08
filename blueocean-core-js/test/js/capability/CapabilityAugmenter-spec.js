/**
 * Created by cmeyers on 9/8/16.
 */
import { assert } from 'chai';

import { CapabilityAugmenter } from '../../../src/js/capability/CapabilityAugmenter';

describe('CapabilityAugmenter', () => {
    let augmenter;

    beforeEach(() => {
        augmenter = new CapabilityAugmenter();
    });

    it('builds the correct augmentation map for a basic tree', () => {
        const pipelines = require('./pipelines-1.json');
        augmenter.augmentCapabilities(pipelines);
    });
});
