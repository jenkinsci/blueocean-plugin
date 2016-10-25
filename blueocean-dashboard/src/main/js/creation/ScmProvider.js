/**
 * Created by cmeyers on 10/21/16.
 */

export default class ScmProvider {

    getDefaultOption() {
        throw new Error('must implement getDefaultOption');
    }

    getCreationFlow() {
        throw new Error('must implement getCreationFlow');
    }

    getRentrantOption() {
        return null;
    }

    getRentrantFlow() {
        return null;
    }

}
