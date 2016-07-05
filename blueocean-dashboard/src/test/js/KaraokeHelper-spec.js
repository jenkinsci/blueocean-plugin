
import { assert } from 'chai';

import {calculateNode } from '../../main/js/util/KaraokeHelper';


describe('KaraokeHelper', () => {
    describe('KaraokeHelper calculateNode', () => {
        const props = { params: {} };
        const nextProps = { params: { node: 21}, result:{} };
        const mergedConfig = {
            node: 32,
            nodeReducer: {
                id:32,
            },
        };
        it('should return an answer if our node param is different (case if some one clicks a flownode)', () => {
            const answer = calculateNode(props, nextProps, mergedConfig);
            assert.notEqual(answer, null);
        });
    });
    describe('calculateNode real life', () => {

    })
});
