import { assert } from 'chai';

import pipelineStore from '../../../main/js/services/PipelineStore';
import pipelineMetadataService  from '../../../main/js/services/PipelineMetadataService';
import { convertJsonToInternalModel }  from '../../../main/js/services/PipelineSyntaxConverter';
import { DragPosition } from "../../../main/js/components/editor/DragPosition";


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

            pipelineStore.moveStep(firstStage, firstStep.id, lastStep.id, DragPosition.BEFORE_ITEM);
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

            pipelineStore.moveStep(firstStage, firstStep.id, firstStage.id, DragPosition.LAST_CHILD);
            assert.equal(firstStage.steps.length, stepCount);
            assert.notEqual(firstStage.steps[0].id, firstStep.id);
            assert.equal(firstStage.steps[2].id, lastStep.id);
            assert.equal(firstStage.steps[3].id, firstStep.id);
        });

        it('should move a step inside block step to ancestor', () => {
            const firstStage = pipelineStore.pipeline.children[0];
            const firstStep = firstStage.steps[0];
            const retryStep = firstStage.steps[2];
            const dragStep = retryStep.children[0];

            pipelineStore.moveStep(firstStage, dragStep.id, firstStep.id, DragPosition.BEFORE_ITEM);
            assert.equal(retryStep.children.length, 2);
            assert.equal(firstStage.steps[0].id, dragStep.id);
            assert.equal(firstStage.steps[1].id, firstStep.id);
        });

        it('should move a step to descendant block step', () => {
            const firstStage = pipelineStore.pipeline.children[0];
            const topLevelStep = firstStage.steps[0];
            const retryStep = firstStage.steps[2];
            const stepCount = retryStep.children.length;
            const nestedStep1 = retryStep.children[0];
            const nestedStep2 = retryStep.children[1];

            pipelineStore.moveStep(firstStage, topLevelStep.id, nestedStep2.id, DragPosition.BEFORE_ITEM);
            assert.equal(retryStep.children.length, stepCount + 1);
            assert.equal(retryStep.children[0].id, nestedStep1.id);
            assert.equal(retryStep.children[1].id, topLevelStep.id);
            assert.equal(retryStep.children[2].id, nestedStep2.id);
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
