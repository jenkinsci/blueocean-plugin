import { BunkerService, Fetch } from '@jenkins-cd/blueocean-core-js';

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
        Fetch.fetch(test._links.stdOut.href)
            .then(
                res => res.text(),
                e => {
                    if (e.response.status === 404) {
                        this.setItem(new Item(test, null, true));
                    }
                }
            )
            .then(data => {
                this.setItem(new Item(test, data, true));
            });
    }

    loadStdErr(test) {
        if (this.hasItem(test)) return;
        Fetch.fetch(test._links.stdErr.href)
            .then(
                res => res.text(),
                e => {
                    if (e.response.status === 404) {
                        this.setItem(new Item(test, null, false));
                    }
                }
            )
            .then(data => {
                this.setItem(new Item(test, data, false));
            });
    }

    getStdOut(test) {
        const item = this.getItem(test._links.stdOut.href);
        return item && item.value;
    }

    getStdErr(test) {
        const item = this.getItem(test._links.stdErr.href);
        return item && item.value;
    }

    bunkerKey(data) {
        const links = data.value.test._links;
        return data.value.isStdOut ? links.stdOut.href : links.stdErr.href;
    }
}
