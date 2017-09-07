import { assert } from 'chai';

import { PipelineGraph } from '../../../src/js/components';
import { layoutGraph } from '../../../src/js/components/pipeline/PipelineGraphLayout';
import { defaultLayout } from '../../../src/js/components/pipeline/PipelineGraphModel';

import { StatusIndicator } from '../../../src/js/components/status/StatusIndicator';

const validResultValues = StatusIndicator.validResultValues;

// Data creation helpers Lifted from stories
let __id = 1111;

function makeNode(name, children = [], state = validResultValues.not_built, completePercent) {
    completePercent = completePercent || ((state == validResultValues.running) ? Math.floor(Math.random() * 60 + 20) : 50);
    const id = __id++;
    return {name, children, state, completePercent, id};
}

function makeSequence(...stages) {
    for (let i = 0; i < stages.length - 1; i++) {
        stages[i].nextSibling = stages[i + 1];
    }

    return stages[0]; // The model only needs the first in a sequence
}

// Assertion helpers
function assertNode(node, text, x, y) {
    assert.ok(node, `node ${text} exists`);
    assert.equal(node.name, text, `node ${text} name`);
    assert.equal(node.x, x, `node ${text} x`);
    assert.equal(node.y, y, `node ${text} y`);
}

function assertSingleNodeRow(row, text, x, y) {
    assert.ok(row, `row for ${text} exists`);
    assert.equal(row.length, 1, `row for ${text} is single node`);
    assertNode(row[0], text, x, y);
}

function assertLabel(labels, text, x,y) {
    const label = labels.find(label => label.text === text);
    assert.ok(label, `label ${text} exists`);
    assert.equal(label.x, x, `label ${text} x`);
    assert.equal(label.y, y, `label ${text} y`);
}

function assertConnection(connections, sourceName, destinationName) {
    for (const compositeConnection of connections) {
        const sourceMatch = compositeConnection.sourceNodes.find(node => node.name === sourceName);
        const destinationMatch = compositeConnection.destinationNodes.find(node => node.name === destinationName);

        if (sourceMatch && destinationMatch) {
            return; // Connection found
        }
    }

    assert.fail(`could not find ${leftText} --> ${rightText} connection`);
}

describe('PipelineGraph', () => {
    describe('layoutGraph', () => {

        it('gracefully handles a Stage with null children', () => {
            const stagesNullChildren = require('../data/pipeline-graph/stages-with-null-children.json');
            const { nodeColumns } = layoutGraph(stagesNullChildren, defaultLayout);
            assert.isOk(nodeColumns);
            assert.equal(nodeColumns.length, 7);
        });

        it('lays out a mixed graph', () => {
            const stages = [
                makeNode('Build', [], validResultValues.success),
                makeNode('Test', [
                    makeNode('JUnit', [], validResultValues.success),
                    makeNode('DBUnit', [], validResultValues.success),
                    makeNode('Jasmine', [], validResultValues.success),
                ]),
                makeNode('Browser Tests', [
                    makeNode('Firefox', [], validResultValues.success),
                    makeNode('Edge', [], validResultValues.failure),
                    makeNode('Safari', [], validResultValues.running, 60),
                    makeNode('Chrome', [], validResultValues.running, 120),
                ]),
                makeNode('Skizzled', [], validResultValues.skipped),
                makeNode('Foshizzle', [], validResultValues.skipped),
                makeNode('Dev', [
                    makeNode('US-East', [], validResultValues.success),
                    makeNode('US-West', [], validResultValues.success),
                    makeNode('APAC', [], validResultValues.success),
                ], validResultValues.success),
                makeNode('Staging', [], validResultValues.skipped),
                makeNode('Production'),
            ];

            const {
                nodeColumns,
                connections,
                bigLabels,
                smallLabels,
                measuredWidth,
                measuredHeight,
            } = layoutGraph(stages, defaultLayout);

            // Basic stuff

            assert.equal(nodeColumns.length, 10, 'column count');
            assert.equal(measuredWidth, 1128, 'measuredWidth');
            assert.equal(measuredHeight, 320, 'measuredHeight');
            assert.equal(smallLabels.length, 10, 'small label count');
            assert.equal(bigLabels.length, 10, 'big label count');

            // Start col
            let col = nodeColumns[0];
            assert.equal(undefined, col.topStage, 'topStage');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Start', 60, 55);

            // End col
            col = nodeColumns[9];
            assert.equal(undefined, col.topStage, 'topStage');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'End', 1068, 55);

            // Col 1
            col = nodeColumns[1];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Build', 'top stage name');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Build', 144, 55);

            // Col 2
            col = nodeColumns[2];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Test', 'top stage name');
            assert.equal(3, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'JUnit', 264, 55);
            assertSingleNodeRow(col.rows[1], 'DBUnit', 264, 125);
            assertSingleNodeRow(col.rows[2], 'Jasmine', 264, 195);

            // Col 3
            col = nodeColumns[3];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Browser Tests', 'top stage name');
            assert.equal(4, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Firefox', 384, 55);
            assertSingleNodeRow(col.rows[1], 'Edge', 384, 125);
            assertSingleNodeRow(col.rows[2], 'Safari', 384, 195);
            assertSingleNodeRow(col.rows[3], 'Chrome', 384, 265);

            // Col 4
            col = nodeColumns[4];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Skizzled', 'top stage name');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Skizzled', 504, 55);

            // Col 5
            col = nodeColumns[5];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Foshizzle', 'top stage name');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Foshizzle', 624, 55);

            // Col 6
            col = nodeColumns[6];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Dev', 'top stage name');
            assert.equal(3, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'US-East', 744, 55);
            assertSingleNodeRow(col.rows[1], 'US-West', 744, 125);
            assertSingleNodeRow(col.rows[2], 'APAC', 744, 195);

            // Col 7
            col = nodeColumns[7];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Staging', 'top stage name');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Staging', 864, 55);

            // Col 8
            col = nodeColumns[8];
            assert.ok(col.topStage, 'topStage');
            assert.equal(col.topStage.name, 'Production', 'top stage name');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Production', 984, 55);

            // Big Labels
            assertLabel(bigLabels, 'Start', 60, 55);
            assertLabel(bigLabels, 'Build', 144, 55);
            assertLabel(bigLabels, 'Test', 264, 55);
            assertLabel(bigLabels, 'Browser Tests', 384, 55);
            assertLabel(bigLabels, 'Skizzled', 504, 55);
            assertLabel(bigLabels, 'Foshizzle', 624, 55);
            assertLabel(bigLabels, 'Dev', 744, 55);
            assertLabel(bigLabels, 'Staging', 864, 55);
            assertLabel(bigLabels, 'Production', 984, 55);
            assertLabel(bigLabels, 'End', 1068, 55);

            // Small Labels - Test
            assertLabel(smallLabels, 'JUnit', 264, 55);
            assertLabel(smallLabels, 'DBUnit', 264, 125);
            assertLabel(smallLabels, 'Jasmine', 264, 195);

            // Small Labels - Browser Tests
            assertLabel(smallLabels, 'Firefox', 384, 55);
            assertLabel(smallLabels, 'Edge', 384, 125);
            assertLabel(smallLabels, 'Safari', 384, 195);
            assertLabel(smallLabels, 'Chrome', 384, 265);

            // Small Labels - Dev
            assertLabel(smallLabels, 'US-East', 744, 55);
            assertLabel(smallLabels, 'US-West', 744, 125);
            assertLabel(smallLabels, 'APAC', 744, 195);

            // Connections
            assertConnection(connections, 'Start', 'Build');

            assertConnection(connections, 'Build', 'JUnit');
            assertConnection(connections, 'Build', 'DBUnit');
            assertConnection(connections, 'Build', 'Jasmine');

            assertConnection(connections, 'JUnit', 'Firefox');
            assertConnection(connections, 'JUnit', 'Edge');
            assertConnection(connections, 'JUnit', 'Safari');
            assertConnection(connections, 'JUnit', 'Chrome');
            assertConnection(connections, 'DBUnit', 'Firefox');
            assertConnection(connections, 'DBUnit', 'Edge');
            assertConnection(connections, 'DBUnit', 'Safari');
            assertConnection(connections, 'DBUnit', 'Chrome');
            assertConnection(connections, 'Jasmine', 'Firefox');
            assertConnection(connections, 'Jasmine', 'Edge');
            assertConnection(connections, 'Jasmine', 'Safari');
            assertConnection(connections, 'Jasmine', 'Chrome');

            assertConnection(connections, 'Firefox', 'US-East');
            assertConnection(connections, 'Firefox', 'US-West');
            assertConnection(connections, 'Firefox', 'APAC');
            assertConnection(connections, 'Edge', 'US-East');
            assertConnection(connections, 'Edge', 'US-West');
            assertConnection(connections, 'Edge', 'APAC');
            assertConnection(connections, 'Safari', 'US-East');
            assertConnection(connections, 'Safari', 'US-West');
            assertConnection(connections, 'Safari', 'APAC');
            assertConnection(connections, 'Chrome', 'US-East');
            assertConnection(connections, 'Chrome', 'US-West');
            assertConnection(connections, 'Chrome', 'APAC');

            assertConnection(connections, 'US-East', 'Production');
            assertConnection(connections, 'US-West', 'Production');
            assertConnection(connections, 'APAC', 'Production');

            assertConnection(connections, 'Production', 'End');

            assert.equal(connections.length, 6, 'Total composite connections');
        });

        it('lays out a multi-stage parallel graph', () => {
            const stages = [
                makeNode("Alpha"),
                makeNode("Bravo", [
                    makeNode("Echo"),
                    makeSequence(
                        makeNode("Foxtrot"),
                        makeNode("Golf"),
                        makeNode("Hotel"),
                    ),
                    makeSequence(
                        makeNode("India"),
                        makeNode("Juliet"),
                    )
                ]),
                makeNode("Charlie"),
            ];

            const {
                nodeColumns,
                connections,
                bigLabels,
                smallLabels,
                measuredWidth,
                measuredHeight,
            } = layoutGraph(stages, defaultLayout);

            // Basic stuff

            assert.equal(nodeColumns.length, 5, 'column count');
            assert.equal(measuredWidth, 768, 'measuredWidth');
            assert.equal(measuredHeight, 250, 'measuredHeight');
            assert.equal(smallLabels.length, 6, 'small label count');
            assert.equal(bigLabels.length, 5, 'big label count');

            // Start col
            let col = nodeColumns[0];
            assert.equal(undefined, col.topStage, 'topStage');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'Start', 60, 55);

            // End col
            col = nodeColumns[4];
            assert.equal(undefined, col.topStage, 'topStage');
            assert.equal(1, col.rows.length);
            assertSingleNodeRow(col.rows[0], 'End', 708, 55);
        })
    });
});
