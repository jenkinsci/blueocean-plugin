import { assert } from 'chai';
import React from 'react';
import sd from 'skin-deep';

import { latestRuns } from './latestRuns';
import RunDetailsArtifacts from '../../main/js/components/RunDetailsArtifacts';

describe('RunDetailsArtifacts', () => {
    let component;
    let tree;
    let output;

    describe('bad data', () => {
        before(() => {
            component = (
                <RunDetailsArtifacts />
            );
            tree = sd.shallowRender(component);
            output = tree.getRenderOutput();
        });

        it('renders nothing', () => {
            assert.isNull(output);
        });
    });

    describe('empty artifacts', () => {
        before(() => {
            component = (
                <RunDetailsArtifacts
                  result={{ artifacts: [] }}
                />
            );
            tree = sd.shallowRender(component);
            output = tree.getRenderOutput();
        });

        it('renders EmptyStateView', () => {
            assert.equal(output.type.name, 'EmptyStateView');
        });
    });

    describe('valid artifacts', () => {
        before(() => {
            const runs = latestRuns.map(run => (run.latestRun));
            component = (
                <RunDetailsArtifacts
                  result={runs[0]}
                />
            );
            tree = sd.shallowRender(component);
            output = tree.getRenderOutput();
        });

        it('renders a Table with expected data', () => {
            assert.equal(output.type.name, 'Table');
            assert.equal(tree.everySubTree('tr').length, 1);

            const cols = tree.subTree('tr').everySubTree('td');
            assert.equal(cols[0].text(), 'hey');
            assert.equal(cols[1].text(), '<FileSize />');
            assert.equal(cols[2].text(), '<Icon />');
        });
    });
});
