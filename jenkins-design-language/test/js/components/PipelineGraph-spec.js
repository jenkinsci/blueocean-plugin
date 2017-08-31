import { assert } from 'chai';

import { PipelineGraph } from '../../../src/js/components';

describe("PipelineGraph", () => {
    describe("createNodeColumns", () => {
        it("gracefully handles a Stage with null children", () => {
            const stagesNullChildren = require('../data/pipeline-graph/stages-with-null-children.json');
            const graph = new PipelineGraph({});
            const columns = graph.createNodeColumns(stagesNullChildren);
            assert.isOk(columns);
            assert.equal(columns.length, 5);

        });
    });
});
