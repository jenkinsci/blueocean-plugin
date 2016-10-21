/**
 * Created by cmeyers on 10/21/16.
 */

export default class ScmProvider {

    getDefaultOption() {
        throw new Error('must implement getDefaultOption');
    }

    getDefaultFlow() {
        throw new Error('must implement getDefaultFlow');
    }

    getRentrantOption() {
        return null;
    }

    getRentrantFlow() {
        return null;
    }

}
