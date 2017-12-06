import { assert } from 'chai';

import pipelineStore from '../../../main/js/services/PipelineStore';
import pipelineMetadataService  from '../../../main/js/services/PipelineMetadataService';
import { convertJsonToInternalModel }  from '../../../main/js/services/PipelineSyntaxConverter';


describe('PipelineStore', () => {
    beforeAll(() => {
        // load pipeline metadata so that "isContainer" value for various steps is set correctly
        pipelineMetadataService.cache.pipelineStepMetadata = JSON.parse(
            require("fs").readFileSync(
                require("path").normalize(__dirname + "/../StepMetadata.json", "utf8")));
    });

    beforeEach(() => {
        const json = require("fs").readFileSync(
            require("path").normalize(__dirname + "/sample-pipeline.json", "utf8")
        );
        const internal = convertJsonToInternalModel(JSON.parse(json));
        pipelineStore.setPipeline(internal);
    });

    describe('moveStep', () => {
        it('should move a step above a later sibling', () => {
            const firstStage = pipelineStore.pipeline.children[0];
            const stepCount = firstStage.steps.length;
            const firstStep = firstStage.steps[0];
            const lastStep = firstStage.steps[3];

            pipelineStore.moveStep(firstStage, firstStep.id, lastStep.id, 'beforeItem');
            assert.equal(firstStage.steps.length, stepCount);
            assert.notEqual(firstStage.steps[0].id, firstStep.id);
            assert.equal(firstStage.steps[2].id, firstStep.id);
            assert.equal(firstStage.steps[3].id, lastStep.id);
        });
        it('should move a step to the end of a stage', () => {
            const firstStage = pipelineStore.pipeline.children[0];
            const stepCount = firstStage.steps.length;
            const firstStep = firstStage.steps[0];
            const lastStep = firstStage.steps[3];

            pipelineStore.moveStep(firstStage, firstStep.id, firstStage.id, 'childItem');
            assert.equal(firstStage.steps.length, stepCount);
            assert.notEqual(firstStage.steps[0].id, firstStep.id);
            assert.equal(firstStage.steps[2].id, lastStep.id);
            assert.equal(firstStage.steps[3].id, firstStep.id);
        });

        // TODO: finish tests

        xit('should move a step to ancestor block step', () => {

        });

        xit('should move a step to descendant block step', () => {

        });
        xit('should throw an exception if trying to move between stages', () => {

        });
    });

    describe('findStepHierarchy', () => {
        it('should return the step and a single ancestor', () => {
            const firstStage = pipelineStore.pipeline.children[0];
            const nestedStep1 = firstStage.steps[2];
            const nestedStep2 = nestedStep1.children[2];

            const hierarchy = pipelineStore.findStepHierarchy(nestedStep2, firstStage.steps);
            assert.equal(hierarchy.length, 2);
            assert.equal(hierarchy[0].id, nestedStep2.id);
            assert.equal(hierarchy[1].id, nestedStep1.id);
        });
    });
});
