console.log('ss');

import { action, observable } from 'mobx';
import { Logger } from '../../../util/Logger';
import { KaraokeApi } from '../index';
import cache from './DataCache';

const log = new Logger('pipeline.run.store.steps');

export class StepStore {
    @observable pending = true;
    @observable steps;
    @observable error;
    url;

    constructor(url) {
        this.setUrl(url);
    }

    setUrl(url) {
        this.url = url;
        this.setSteps(cache.get(this.url) || {
            _links: {
                self: {
                    href: this.url,
                },
            },
        });
    }

    @action
    setSteps(steps) {
        cache.put(this.url, steps);
        this.steps = steps;
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
        log.debug('fetching steps', this.url);
        this.setPending(true);
        return KaraokeApi.getSteps(this.url)
            .then(result => {
                const stepData = {
                    _links: {
                        self: {
                            href: this.url,
                        },
                    },
                };
                stepData.data = result;
                this.setSteps(stepData);
            }).catch(err => {
                log.error('Error fetching steps', err);
                this.setError(err);
            }).then(() => {
                this.setPending(false);
            });
    }
}
