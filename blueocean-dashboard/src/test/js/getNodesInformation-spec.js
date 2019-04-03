import { nodesNamedSequentialParralels } from './data/runs/nodes/runNodes-named-sequential-parallels';
import { getNodesInformation } from '../../main/js/components/karaoke/rest/getNodesInformation';
import { assert } from 'chai';

import { mockExtensionsForI18n } from './mock-extensions-i18n';

describe('getNodesInformation', () => {

    beforeAll(() => {
        mockExtensionsForI18n();
    });

    describe('correctly identifies graph structures', () => {

        it('nodesNamedSequentialParralels', () => {

            const nodes = getNodesInformation(nodesNamedSequentialParralels).model;

            function getNode(named) {
                // console.log('getNode', named);         // TODO: RM
                const result = nodes.find(node => {
                    // console.log('\tnode.displayName', node.displayName);             // TODO: RM
                    return node.displayName === named;
                });
                // console.log('\tresult =',  typeof result);
                return result;
            }

            // First top-level stage
            let node = getNode('Alpha');
            assert(node, 'node exists');
            assert.equal(node.id, 6, 'node id');
            assert.equal(node.firstParent, null, 'node first parent');
            assert.equal(node.parent, null, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, false, 'isSequential');

            // Second top-level stage
            node = getNode('Bravo');
            assert(node, 'node exists');
            assert.equal(node.id, 11, 'node id');
            assert.equal(node.firstParent, 6, 'node first parent');
            assert.equal(node.parent, 6, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, false, 'isSequential');

            // First parallel (Delta)
            node = getNode('Delta');
            assert(node, 'node exists');
            assert.equal(node.id, 16, 'node id');
            assert.equal(node.firstParent, 11, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, true, 'isParallel');
            assert.equal(node.isSequential, false, 'isSequential');

            // Second parallel (hotel, indigo)
            node = getNode('Hotel');
            assert(node, 'node exists');
            assert.equal(node.id, 29, 'node id');
            assert.equal(node.firstParent, 17, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            // TODO: add and test for branch label

            node = getNode('Indigo');
            assert(node, 'node exists');
            assert.equal(node.id, 48, 'node id');
            assert.equal(node.firstParent, 29, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            // TODO: add and test for branch label

            // TODO: third parallel (foxtrot, juliet keeeeeelo)
            // TODO: fourth parallel (golf)

            // Third top-level stage
            node = getNode('Charlie');
            assert(node, 'node exists');
            assert.equal(node.id, 75, 'node id');
            assert.equal(node.firstParent, null, 'node first parent');  // Not sure why, but this is what the backend does for now
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, false, 'isSequential');
        });

    });

});
