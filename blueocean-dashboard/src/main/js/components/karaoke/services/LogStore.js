console.log('ls');

import { action, observable } from 'mobx';
import { Logger } from '../../../util/Logger';
import { KaraokeApi } from '../index';

const log = new Logger('pipeline.run.store.log');

export class LogStore {
    @observable pending = false;
    @observable log;
    @observable error;
    url;

    constructor(url, start = null) {
        this.url = url;
        this.setLog({
            _links: {
                self: {
                    href: this.url,
                },
            },
            data: [],
        });
        this.log.start = start; // don't want mobx to manage this field
    }

    @action
    setLog(l) {
        this.log = l;
    }

    @action
    setLogData(logData) {
        this.log.data = logData;
    }

    @action
    setError(error) {
        this.error = error;
    }

    @action
    setPending(pending) {
        this.pending = pending;
    }

    /**
     * Fetches the detail from the backend and set the data
     * @returns {Promise}
     */
    fetch() {
        log.debug('fetching log', this.url, 'start:', this.log.start);
        this.setPending(true);
        return KaraokeApi.getGeneralLog(this.url, { start: this.log.start })
            .then(response => {
                const { newStart, hasMore } = response;
                this.log.hasMore = hasMore;
                this.log.newStart = newStart;
                log.debug('log response', response, this.log);
                return response.text(); // this is a Promise
            })
            .then(text => {
                if (text && text.trim) {
                    const lines = text.trim().split('\n');
                    if (this.log.newStart) {
                        this.setLogData(this.log.data.concat(lines));
                    } else {
                        this.setLogData(lines);
                    }
                }
            }).catch(err => {
                log.error('Error fetching log', err);
                this.setError(err);
            }).then(() => this.setPending(false));
    }
}
