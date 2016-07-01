/**
 * Simple pipeline branch API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipeline branches, encapsulating REST API calls etc.
 */

import Pipeline from './Pipeline';

export default class Branch extends Pipeline {

    constructor(organization, pipelineName, branchName, url) {
        super(organization, pipelineName, url);
        this.branchName = branchName;
        if (!url) {
            this.url = `/rest/organizations/${this.organization}/pipelines/${this.pipelineName}/branches/${this.branchName}`;
        }
    }

    equals(branch) {
        if (branch && branch.branchName === this.branchName) {
            // and it's the same pipeline...
            return super.equals(branch);
        }
        return false;
    }
}
