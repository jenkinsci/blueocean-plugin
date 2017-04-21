import { Fetch } from '@jenkins-cd/blueocean-core-js';
import { BunkerService } from '../../../../../../blueocean-core-js/src/js/services/BunkerService';

class Item {
    constructor(test, log, isStdOut) {
        this.test = test;
        this.log = log;
        this.isStdOut = isStdOut;
    }
}

export default class TestLogService extends BunkerService {

    loadStdOut(test) {
        if (this.hasItem(test)) return;
        Fetch.fetch(test._links.stdout.href)
            .then(res => res.text())
            .then((data) => {
                this.setItem(new Item(test, data, true));
            })
            .catch(TestLogService.ignoreNotFound);
    }

    loadStdErr(test) {
        if (this.hasItem(test)) return;
        Fetch.fetch(test._links.stderr.href)
            .then(res => res.text())
            .then((data) => {
                this.setItem(new Item(test, data, false));
            })
            .catch(TestLogService.ignoreNotFound);
    }

    getStdOut(test) {
        console.log('get out');
        const item = this.getItem(test._links.stdout.href);
        return item && item.value && item.value.log;
    }

    getStdErr(test) {
        console.log('get err');
        const item = this.getItem(test._links.stderr.href);
        return item && item.value && item.value.log;
    }

    bunkerKey(data) {
        const links = data.value.test._links;
        return data.value.isStdOut ? links.stdout.href : links.stderr.href;
    }

    setItem(item) {
        console.log('setting item');
        super.setItem(item);
    }

    static ignoreNotFound(e) {
        if (e.response.status !== 404) {
            throw e;
        }
    }
}
