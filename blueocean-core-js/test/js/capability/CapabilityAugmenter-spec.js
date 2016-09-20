/**
 * Created by cmeyers on 9/8/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { assert } from 'chai';

import { CapabilityAugmenter } from '../../../src/js/capability/CapabilityAugmenter';
import { capable } from '../../../src/js/capability/Capable';

class MockCapabilityStore {

    constructor() {
        this.classMap = {};
    }

    resolveCapabilities(... classNames) {
        const result = {};

        for (const className of classNames) {
            result[className] = this.classMap[className];
        }

        return new Promise(resolve => resolve(result));
    }

    addCapability(className, ... capabilities) {
        this.classMap[className] = [className].concat(capabilities);
    }
}


describe('CapabilityAugmenter', () => {
    let mockCapabilityStore;
    let augmenter;

    beforeEach(() => {
        mockCapabilityStore = new MockCapabilityStore();
        augmenter = new CapabilityAugmenter(mockCapabilityStore);
    });

    describe('augmentCapabilities', () => {
        it('adds capabilities to a single multibranch pipeline', (done) => {
            mockCapabilityStore.addCapability(
                'io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl',
                'jenkins.branch.MultiBranchProject'
            );

            const multibranch = require('./multibranch-1.json');
            augmenter.augmentCapabilities(multibranch)
                .then(data => {
                    assert.isOk(data._capabilities);
                    assert.equal(data._capabilities.length, 2);
                    done();
                });
        });

        it('adds capabilities to two branches from a multibranch pipeline', (done) => {
            mockCapabilityStore.addCapability(
                'io.jenkins.blueocean.rest.impl.pipeline.BranchImpl',
                'io.jenkins.blueocean.rest.model.BlueBranch', 'org.jenkinsci.plugins.workflow.job.WorkflowJob',
                'io.jenkins.blueocean.rest.impl.pipeline.PullRequest',
            );
            mockCapabilityStore.addCapability(
                'io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl',
                'org.jenkinsci.plugins.workflow.job.WorkflowRun'
            );
            mockCapabilityStore.addCapability(
                'io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl$1'
            );

            const branches = require('./branches-1.json');
            augmenter.augmentCapabilities(branches)
                .then(data => {
                    assert.equal(data.length, 2);
                    const branch1 = data[0];
                    assert.isOk(branch1._capabilities);
                    assert.equal(branch1._capabilities.length, 4);
                    assert.isOk(branch1.latestRun._capabilities);
                    assert.equal(branch1.latestRun._capabilities.length, 2);
                    assert.isOk(branch1.latestRun.artifacts);
                    assert.isOk(branch1.latestRun.artifacts[0]._capabilities);
                    assert.equal(branch1.latestRun.artifacts[0]._capabilities.length, 1);

                    const branch2 = data[0];
                    assert.isOk(branch2._capabilities);
                    assert.equal(branch2._capabilities.length, 4);
                    assert.isOk(branch2.latestRun._capabilities);
                    assert.equal(branch2.latestRun._capabilities.length, 2);
                    assert.isOk(branch2.latestRun.artifacts);
                    assert.isOk(branch2.latestRun.artifacts[0]._capabilities);
                    assert.equal(branch2.latestRun.artifacts[0]._capabilities.length, 1);
                    done();
                });
        });

        it('makes the "can" convenience method available', (done) => {
            mockCapabilityStore.addCapability(
                'io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl',
                'jenkins.branch.MultiBranchProject'
            );

            const multibranch = require('./multibranch-1.json');
            augmenter.augmentCapabilities(multibranch)
                .then(data => {
                    assert.isTrue(capable(data, 'jenkins.branch.MultiBranchProject'));
                    assert.isFalse(capable(data, 'jenkins.not.real.Capability'));
                    done();
                });
        });

        it('initializes an empty array for an unknown capability', (done) => {
            const unknown = { _class: 'foo.bar.Unknown' };
            augmenter.augmentCapabilities(unknown)
                .then(data => {
                    assert.isOk(data._capabilities);
                    assert.equal(data._capabilities.length, 0);
                    done();
                });
        });
    });

    describe('_findClassesInTree', () => {
        it('builds the correct map for pipelines list', () => {
            const pipelines = require('./pipelines-1.json');
            const classMap = augmenter._findClassesInTree(pipelines);

            assert.equal(Object.keys(classMap).length, 31);

            const matrix = classMap['io.jenkins.blueocean.rest.impl.pipeline.MatrixProjectImpl'];
            assert.isOk(matrix);
            assert.equal(matrix.length, 1);
            const multibranch = classMap['io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl'];
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

            assert.equal(Object.keys(classMap).length, 42);

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

            assert.equal(Object.keys(classMap).length, 5);

            const multibranch1 = classMap['io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl'];
            assert.isOk(multibranch1);
            assert.equal(multibranch1.length, 1);

            // check the 'actions' capabilities too
            // eslint-disable-next-line max-len
            const action1 = classMap['com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider$FolderCredentialsProperty$CredentialsStoreActionImpl'];
            assert.isOk(action1);
            assert.equal(action1.length, 1);
            const action2 = classMap['com.cloudbees.hudson.plugins.folder.relocate.RelocationAction'];
            assert.isOk(action2);
            assert.equal(action2.length, 1);
            const action3 = classMap['com.cloudbees.plugins.credentials.ViewCredentialsAction'];
            assert.isOk(action3);
            assert.equal(action3.length, 1);
            const action4 = classMap['org.jenkinsci.plugins.workflow.cps.Snippetizer$LocalAction'];
            assert.isOk(action4);
            assert.equal(action4.length, 1);
        });

        it('handles cycles in the data', () => {
            const root = { _class: 'foo.Bar' };
            root.cycle = root;

            const classMap = augmenter._findClassesInTree(root);

            assert.equal(Object.keys(classMap).length, 1);

            const bar = classMap['foo.Bar'];
            assert.isOk(bar);
            assert.equal(bar.length, 1);
        });
    });
});
