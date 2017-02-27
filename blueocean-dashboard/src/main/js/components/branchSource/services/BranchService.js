import { BunkerService } from '@jenkins-cd/blueocean-core-js';
import { BranchPager } from './BranchPager';
/*
 * This class provides a simple branch service.
 *
 * @export
 * @class BranchService
 * @extends {BunkerService}
 */
export class BranchService extends BunkerService {
    /**
     * Gets the Branch pager
     *
     * @param {string} url we are requesting
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
