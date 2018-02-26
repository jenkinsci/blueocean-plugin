import { observable, action, computed } from 'mobx';
import { Fetch } from '../fetch';

/**
 * Provide a pagination function for the generic
 * blueocean pagination
 *
 * @export
 * @param {string} url - Base url to paginate.
 * @returns {function} - Function that provides paginated urls.
 */
export function paginateUrl(url) {
    const sep = url.indexOf('?') >= 0 ? '&' : '?';
    return (start, limit) => `${url}${sep}start=${start}&limit=${limit}`;
}


/**
 * The pager fetches pages of data from the BlueOcean api. It fetches pages of data, then
 * inserts them into the [@link BunkerService], and stores the href from the data.
 *
 * MobX computes a data field from the hrefs backed by the backend cache. This allows for SSE events
 * to be propagated to the pager.
 *
 * @export
 * @class Pager
 */
export class Pager {
    /**
     * List of displayed items hrefs.
     */
    @observable hrefs = [];
    /**
     * pager is fetching data.
     */
    @observable pending = false;
    /**
     * Will be set in an error occurs.
     */
    @observable error = null;
    /**
     * The latest page the pager has fetched.
     */
    @observable currentPage = 0;
    /**
     * More pages to fetch.
     */
    @observable hasMore = true;

    /**
     * Mobx computed value that creates an array of objects from the list of hrefs stored. If either the
     * bunker changes, or the hrefs change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {Array<Object>}
     */
    @computed
    get data() {
        return this.hrefs.map(href => this.bunker.getItem(href)).filter(item => item !== undefined);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {string} url - Base url of collection to fetch
     * @param {number} pageSize - Page size to fetch during one load.
     * @param {BunkerService} bunker - Data store
     * @param {UrlProvider} [urlProvider=paginateUrl]
     */
    constructor(url, pageSize, bunker, urlProvider = paginateUrl) {
        this.pageSize = pageSize;
        this.url = url;
        this.urlProvider = urlProvider;
        this.pagedUrl = this.urlProvider(url);
        this.pageSize = pageSize;
        this.bunker = bunker;

        // Fetch the first page so that the user does not have to.
        this.fetchNextPage();
    }

    /**
     * Fetches the next page from the backend.
     *
     * @returns {Promise}
     */
    @action
    fetchNextPage() {
        // Get the next page's url.'
        const url = this.pagedUrl(this.currentPage * this.pageSize, this.pageSize + 1);

        this.pending = true;

        return Fetch.fetchJSON(url)
            .then(action('Process pager data', data => {
                // Store item in bunker.
                const saved = this.bunker.setItems(data);

                // 1 extra item is fetched because need to know if there are more packages. So
                // slice off the last item, then map all items to just be hrefs.
                const trimmedHrefs = saved.slice(0, this.pageSize).map(item => item._links.self.href);

                // Append the new Hrefs to the existing ones.
                this.hrefs = this.hrefs.concat(trimmedHrefs);

                // True if we fetch more items than the page size.
                this.hasMore = data.length > this.pageSize;
                this.currentPage = this.currentPage + 1;
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    /**
     * Refreshes the Hrefs for the pager. It also stores the latest data in the [@link BunkerService]
     *
     * This might be called if something like sorting of a list changes.
     *
     * @returns {Promise}
     */
    @action
    refresh() {
        const url = this.pagedUrl(0, this.currentPage * this.pageSize + 1);
        this.pending = true;
        return Fetch.fetchJSON(url) // Fetch data
            .then(action('set data', data => {
                this.bunker.setItems(data);
                this.hrefs = data.slice(0, this.pageSize).map(x => x._links.self.href);
                this.hasMore = data.length > this.pageSize;
                this.currentPage = this.currentPage + 1;
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                this.err = err;
            });
    }

    /**
     * Inserts an href into the list. This will cause a reaction render for the paged list of data.
     *
     * @param {string} href - href of item to display
     * @param {number} [pos=0] - Position to insert it. Default is first item.
     */
    @action
    insert(href, pos = 0) {
        this.hrefs.splice(pos, 0, href);
    }

    /**
     * Removes an href into the list. This will cause a reaction render for the paged list of data.
     *
     * @param {string} href - href of item to remove
     */
    @action
    remove(href) {
        const idx = this.hrefs.indexOf(href);
        this.hrefs.splice(idx, 1);
    }

    /**
     * Href exists in pager.
     *
     * @param {string} href
     * @returns {boolean} - True if this pager does have this href
     */
    has(href) {
        return this.hrefs.indexOf(href) > -1;
    }
}
