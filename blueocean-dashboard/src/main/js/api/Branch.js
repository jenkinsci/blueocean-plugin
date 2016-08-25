/**
 * Simple pipeline branch API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipeline branches, encapsulating REST API calls etc.
 */

import Pipeline from './Pipeline';

const TYPE = 'Branch';
export default class Branch extends Pipeline {

    constructor(organization, pipelineName, branchName, url) {
        super(organization, pipelineName, url);
        this._type = TYPE;
        this.branchName = branchName;
        if (!url) {
            this.url = `/rest/organizations/${this.organization}/pipelines/${this.pipelineName}/branches/${this.branchName}`;
        }
    }

    equals(branch) {
        if (branch && branch._type === TYPE && this._compareBranchNames(branch.branchName, this.branchName)) {
            // and it's the same pipeline...
            return (
                branch.organization === this.organization &&
                branch.pipelineName === this.pipelineName
            );
        }
        return false;
    }

    /**
     * Check if two branch names are the same.
     * Works around encoding mismatch bugs by encoding each branch name and comparing to the other.
     * @param branchName1
     * @param branchName2
     * @returns {boolean}
     * @private
     */
    _compareBranchNames(branchName1, branchName2) {
        return branchName1 === branchName2 ||
            encodeURIComponent(branchName1) === branchName2 ||
            branchName1 === encodeURIComponent(branchName2);
    }
}
