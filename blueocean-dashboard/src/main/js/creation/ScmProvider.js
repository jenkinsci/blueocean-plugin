/**
 * Created by cmeyers on 10/17/16.
 */

export default class ScmProvider {

    getDisplayName() {
        throw new Error("must implement 'getDisplayName()'");
    }

    getComponentName() {
        throw new Error("must implement 'getComponent()");
    }

}
