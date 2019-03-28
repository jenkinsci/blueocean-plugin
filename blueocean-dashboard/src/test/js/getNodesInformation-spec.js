import {nodesNamedSequentialParralels} from './data/runs/nodes/runNodes-named-sequential-parallels';
import {getNodesInformation} from '../../main/js/components/karaoke/rest/getNodesInformation';
import { assert } from 'chai';

describe('getNodesInformation', () => {

    it('handles nodesNamedSequentialParralels', () => {

        const nodes = getNodesInformation(nodesNamedSequentialParralels).model;        // TODO: RM

        console.log('\n\n\n\n\n' + JSON.stringify(nodes) + '\n\n\n\n\n');                 // TODO: RM

        function getNode(named) {
            console.log('getNode',named);
            return nodes.find(node => {
                console.log('\tnode.displayName',node.displayName);             // TODO: RM
                return node.displayName === named
            });
        }

        // First top-level stage
        let node = getNode('Alpha');
        assert(node, 'node exists');
        assert.equal(node.id, 6, 'node id');
        assert.equal(node.firstParent, null, 'node first parent');
        assert.equal(node.parent, null, 'node parent');

        // Second top-level stage
        node = getNode('Bravo');
        assert(node, 'node exists');
        assert.equal(node.id, 11, 'node id');
        assert.equal(node.firstParent, 6, 'node first parent');
        assert.equal(node.parent, 6, 'node parent');

        // First parallel (echo, hotel, indigo)
        node = getNode('Echo');
        assert(node, 'node exists');
        assert.equal(node.id, 17, 'node id');
        assert.equal(node.firstParent, 11, 'node first parent');
        assert.equal(node.parent, 11, 'node parent');

        node = getNode('Hotel');
        assert(node, 'node exists');
        assert.equal(node.id, 29, 'node id');
        assert.equal(node.firstParent, 17, 'node first parent');
        assert.equal(node.parent, 11, 'node parent');

        node = getNode('Indigo');
        assert(node, 'node exists');
        assert.equal(node.id, 48, 'node id');
        assert.equal(node.firstParent, 29, 'node first parent');
        assert.equal(node.parent, 11, 'node parent');

        // TODO: second parallel (foxtrot, juliet keeeeeelo)
        // TODO: third parallel (golf)

        // Third top-level stage
        node = getNode('Charlie');
        assert(node, 'node exists');
        assert.equal(node.id, 75, 'node id');
        assert.equal(node.firstParent, null, 'node first parent');  // Not sure why, but this is what the backend does for now
        assert.equal(node.parent, null, 'node parent');
    });

});
