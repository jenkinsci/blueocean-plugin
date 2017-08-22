import { convertJsonToInternalModel, convertInternalModelToJson }  from '../../../main/js/services/PipelineSyntaxConverter';
import pipelineMetadataService  from '../../../main/js/services/PipelineMetadataService';
import { assert } from 'chai';

describe('Pipeline Syntax Converter', () => {
    before(() => {
        pipelineMetadataService.cache.pipelineStepMetadata = JSON.parse(
            require("fs").readFileSync(
                require("path").normalize(__dirname + "/../StepMetadata.json", "utf8")));
    });

    after(() => {
        delete pipelineMetadataService.stepData;
    });
    
    it('converts from JSON: agent any', () => {
        const p = {
            "pipeline": {
                "agent": {
                    "isLiteral": true,
                    "value": "any"
                },
                "stages": []
            }};
        const internal = convertJsonToInternalModel(p);
        assert(internal.agent.value == 'any', "Wrong agent");
    });

    it('converts from JSON: agent docker', () => {
        const p = {
            "pipeline": {
                "agent": [  {
                    "key": "docker",
                    "value": {
                        "isLiteral": true,
                        "value": "httpd:2.4.12"
                    }
                }],
                "stages": []
            }};
        const internal = convertJsonToInternalModel(p);
        assert(internal.agent[0].key == 'docker', "Wrong agent");
    });
    
    it('converts from JSON: single stage', () => {
        const p = {"pipeline": {
            "stages": [  {
                "name": "foo",
                "branches": [{
                    "name": "default",
                    "steps": [{
                        "name": "sh",
                        "arguments": {
                            "isLiteral": true,
                            "value": "echo \"THIS WORKS\""
                        }
                    }]
                }]
            }],
            "agent": {
                "isLiteral": true,
                "value": "any"
            }
        }};
        const internal = convertJsonToInternalModel(p);
        assert(internal.children[0].children.length == 0, "Single stage conversion failed");
        assert(internal.children[0].steps.length == 1, "Steps not at correct stage");
        assert(internal.children[0].name == 'foo', "Wrong stage name");
    });

    it('converts from JSON: parallel stage', () => {
        const p = {"pipeline": {
            "stages": [  {
                "name": "parallel test",
                "branches": [{
                    "name": "branch 1",
                    "steps": [{
                        "name": "echo",
                        "arguments": {
                            "isLiteral": true,
                            "value": "this is branch 1"
                        }
                    }]
                }, {
                    "name": "branch 2",
                    "steps": [{
                        "name": "echo",
                        "arguments": {
                            "isLiteral": true,
                            "value": "this is branch 2"
                        }
                    }]
                }]
            }],
            "agent": {
                "isLiteral": true,
                "value": "any"
            }
        }};

        const internal = convertJsonToInternalModel(p);
        assert(internal.children[0].children.length == 2, "Stages not parallel");
        assert(internal.children[0].steps.length == 0, "Steps not at correct stage");
        assert(internal.children[0].children[0].name == 'branch 1', "Wrong stage name");
        assert(internal.children[0].children[1].name == 'branch 2', "Wrong stage name");
    });
    
    it('converts from JSON: named parameter values properly', () => {
        const p = {"pipeline": {
            "stages": [  {
                "name": "foo",
                "branches": [{
                    "name": "default",
                    "steps": [{
                        "name": "bat",
                        "arguments": [{
                            "key": "script",
                            "value": {
                                "isLiteral": true,
                                "value": "someBatScript"
                            },
                        },{
                            "key": "returnStdout",
                            "value": {
                                "isLiteral": true,
                                "value": true
                            }
                        }]
                    }]
                }]
            }],
            "agent": {
                "isLiteral": true,
                "value": "any"
            }
        }};
        const internal = convertJsonToInternalModel(p);
        const batStep = internal.children[0].steps[0];
        assert(batStep.name == 'bat', "Incorrect step function");
        // 'script' is the required parameter
        assert(batStep.data.script == 'someBatScript', "Named arguments not properly handled");
        assert(batStep.data.returnStdout == true, "Named arguments not properly handled");
    });
    
    it('converts from JSON: unnamed parameter values properly', () => {
        const p = {"pipeline": {
            "stages": [  {
                "name": "foo",
                "branches": [{
                    "name": "default",
                    "steps": [{
                        "name": "bat",
                        "arguments": {
                            "isLiteral": true,
                            "value": "someBatScript"
                        }
                    }]
                }]
            }],
            "agent": {
                "isLiteral": true,
                "value": "any"
            }
        }};
        const internal = convertJsonToInternalModel(p);
        const batStep = internal.children[0].steps[0];
        assert(batStep.name == 'bat', "Incorrect step function");
        // 'script' is the required parameter
        assert(batStep.data.script == 'someBatScript', "Single required argument not properly handled");
    });

    it('converts from JSON: nested steps', () => {
        const p = {"pipeline": {
            "stages": [{"name": "multiple arguments",
            "branches": [{
                "name": "default","steps": [{
                    "name": "timeout","arguments": [
                        {"key": "time","value": {"isLiteral": true,"value": 5}},
                        {"key": "unit","value": {"isLiteral": true,"value": "SECONDS"}}],
                        "children": [{"name": "echo","arguments": 
                        {"isLiteral": true,"value": "hello"}}]}]}]}],
                        "agent": {"isLiteral": true,"value": "any"}}};
        const internal = convertJsonToInternalModel(p);
        const containerStep = internal.children[0].steps[0];
        assert(containerStep.name == 'timeout', "Incorrect step function");
        // 'script' is the required parameter
        assert(containerStep.children.length == 1, "No children for nested step");
    });

    it('converts to JSON: basic', () => {
        const internal: Pipeline = {
            children: [
                {
                    name: "stage 1",
                    steps: [
                        {
                            functionName: 'sh',
                            data: {
                                script: 'echo hello',
                            }
                        }
                    ]
                },
            ]
        };
        const out = convertInternalModelToJson(internal);
        assert(out.pipeline.
            stages[0].
            branches[0].
            steps[0].
            arguments[0].
            key == 'script', "Incorrect conversion to JSON");
    });

    it('restores unknown sections from JSON', () => {
        const p = {
            "pipeline": {
                "agent": [  {
                    "key": "docker",
                    "value": {
                        "isLiteral": true,
                        "value": "httpd:2.4.12"
                    }
                }],
                "stages": [  {
                    "name": "foo",
                    "branches": [{
                        "name": "default",
                        "steps": [{
                            "name": "bat",
                            "arguments": {
                                "isLiteral": true,
                                "value": "someBatScript"
                            }
                        }],
                    }],
                    "stageUnknownSection": {
                        "someStageKey": "someStageValue",
                    }
                }],
                "someUnkownSection": {
                    "someKey": "someValue",
                }
            }};
        const internal = convertJsonToInternalModel(p);
        assert(internal.someUnkownSection, "Internal unknown section not saved");
        assert(internal.
            children[0].
            stageUnknownSection, "Internal stage unknown section not saved");

        const out = convertInternalModelToJson(internal);
        assert(out.pipeline.someUnkownSection, "Unknown section not restored");
        assert(out.pipeline.
            stages[0].
            stageUnknownSection, "Stage unknown section not restored");
    });

    it('handles unknown steps', () => {
        const p = {"pipeline": {
            "stages": [{"name": "stage 1",
            "branches": [{
                "name": "default","steps": [{
                    "name": "unknownStep","arguments": [
                        {"key": "someArgument","value": {"isLiteral": true,"value": 5}},],
                       }]}]}],
            "agent": {"isLiteral": true,"value": "any"}}};
        const internal = convertJsonToInternalModel(p);
        const unknownStep = internal.children[0].steps[0];
        assert(unknownStep.name == 'unknownStep', "Unknown step not converted");

        const out = convertInternalModelToJson(internal);
        const containerStep = out.pipeline.
            stages[0].
            branches[0].
            steps[0];
        assert(containerStep.
            name == 'unknownStep', "Unknown step not restored");
        assert(containerStep.
            arguments[0].
            key == 'someArgument', "Unknown step arguments not restored");
    });
    
});
