/**
 * Simple pipeline API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipelines, encapsulating REST API calls etc.
 */

export default class Pipeline {
    constructor(organization, pipelineName) {
        this.organization = organization;
        this.name = pipelineName;
    }
}
