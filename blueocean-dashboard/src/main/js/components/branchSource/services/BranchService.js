import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { BranchPager } from './BranchPager';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Service');
/*
 * This class provides karaoke related services.
 *
 * @export
 * @class BranchService
 * @extends {BunkerService}
 */
export class BranchService extends BunkerService {
    /**
     * Gets the Branch pager
     *
     * @param {object} pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @returns {Pager} Pager for this pipelne.
     */
    branchPager(url) {
        return this.pagerService.getPager({
            key: url,
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new BranchPager(this, url),
        });
    }
    /**
     * Gets a detail from the store.
     *
     */
    getDetail(href) {
        return this.getItem(href);
    }

    setItems(items) {
        this.setItem(items);
    }

}
