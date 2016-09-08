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

    describe('_findClassesInTree', () => {
        it('builds the correct map for pipelines list', () => {
            const pipelines = require('./pipelines-1.json');
            const classMap = augmenter._findClassesInTree(pipelines);

            assert.equal(Object.keys(classMap).length, 6);

            const matrix = classMap['io.jenkins.blueocean.rest.impl.pipeline.MatrixProjectImpl']
            assert.isOk(matrix);
            assert.equal(matrix.length, 1);
            const multibranch = classMap['io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl']
            assert.isOk(multibranch);
            assert.equal(multibranch.length, 3);
            const pipeline = classMap['io.jenkins.blueocean.rest.impl.pipeline.PipelineImpl'];
            assert.isOk(pipeline);
            assert.equal(pipeline.length, 4);
            const pipelineRun = classMap['io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl'];
            assert.isOk(pipelineRun);
            assert.equal(pipelineRun.length, 3);
            const abstractPipeline = classMap['io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl'];
            assert.isOk(abstractPipeline);
            assert.equal(abstractPipeline.length, 4);
            const freestyleRun = classMap['io.jenkins.blueocean.service.embedded.rest.FreeStyleRunImpl'];
            assert.isOk(freestyleRun);
            assert.equal(freestyleRun.length, 4);
        });

        it('builds the correct map for favorites list', () => {
            const favorites = require('./favorites-1.json');
            const classMap = augmenter._findClassesInTree(favorites);

            assert.equal(Object.keys(classMap).length, 7);

            const branch = classMap['io.jenkins.blueocean.rest.impl.pipeline.BranchImpl'];
            assert.isOk(branch);
            assert.equal(branch.length, 4);
            const pipeline = classMap['io.jenkins.blueocean.rest.impl.pipeline.PipelineImpl'];
            assert.isOk(pipeline);
            assert.equal(pipeline.length, 3);
            const pipelineRun = classMap['io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl'];
            assert.isOk(pipelineRun);
            assert.equal(pipelineRun.length, 6);
            const abstractPipeline = classMap['io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl'];
            assert.isOk(abstractPipeline);
            assert.equal(abstractPipeline.length, 4);
            const abstractRun = classMap['io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl$1'];
            assert.isOk(abstractRun);
            assert.equal(abstractRun.length, 1);
            const favorite = classMap['io.jenkins.blueocean.service.embedded.rest.FavoriteImpl'];
            assert.isOk(favorite);
            assert.equal(favorite.length, 11);
            const freestyleRun = classMap['io.jenkins.blueocean.service.embedded.rest.FreeStyleRunImpl'];
            assert.isOk(freestyleRun);
            assert.equal(freestyleRun.length, 4);
        });

        it('builds the correct map for a multibranch pipeline', () => {
            const multibranch = require('./multibranch-1.json');
            const classMap = augmenter._findClassesInTree(multibranch);

            assert.equal(Object.keys(classMap).length, 1);

            const multibranch1 = classMap['io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl']
            assert.isOk(multibranch1);
            assert.equal(multibranch1.length, 1);
        });

    });
});
