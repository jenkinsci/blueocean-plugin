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
                const result = nodes.find(node => {
                    return node.displayName === named;
                });
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

            // Second parallel (Echo, hotel, indigo)
            node = getNode('Hotel');
            assert(node, 'node exists');
            assert.equal(node.id, 29, 'node id');
            assert.equal(node.firstParent, 17, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            assert.equal(node.seqContainerName, 'Echo', 'seqContainerName');

            node = getNode('Indigo');
            assert(node, 'node exists');
            assert.equal(node.id, 48, 'node id');
            assert.equal(node.firstParent, 29, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            assert.equal(node.seqContainerName, '', 'seqContainerName');  // TODO: this should be Echo also I think. See if it breaks shit.

            // Third parallel (foxtrot, juliet keeeeeelo)
            node = getNode('Juliet');
            assert(node, 'node exists');
            assert.equal(node.id, 31, 'node id');
            assert.equal(node.firstParent, 18, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            assert.equal(node.seqContainerName, 'Foxtrot', 'seqContainerName');

            node = getNode('Keeeeeelo');
            assert(node, 'node exists');
            assert.equal(node.id, 50, 'node id');
            assert.equal(node.firstParent, 31, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, false, 'isParallel');
            assert.equal(node.isSequential, true, 'isSequential');
            assert.equal(node.seqContainerName, '', 'seqContainerName');  // TODO: this should be Foxtrot also I think. See if it breaks shit.

            // Fourth parallel (golf)
            node = getNode('Golf');
            assert(node, 'node exists');
            assert.equal(node.id, 19, 'node id');
            assert.equal(node.firstParent, 11, 'node first parent');
            assert.equal(node.parent, 11, 'node parent');
            assert.equal(node.isParallel, true, 'isParallel');
            assert.equal(node.isSequential, false, 'isSequential');
            assert.equal(node.seqContainerName, '', 'seqContainerName');

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
