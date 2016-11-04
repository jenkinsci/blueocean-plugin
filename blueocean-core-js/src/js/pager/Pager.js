import { observable, action, computed } from 'mobx';
import { Fetch } from '../fetch';
/**
 * Provide a pagination function for the generic
 * blueocean pagination
 */
export function paginateUrl(url) {
    const sep = url.indexOf('?') >= 0 ? '&' : '?';
    return (start, limit) => `${url}${sep}start=${start}&limit=${limit}`;
}

export default class Pager {

    @observable hrefs = [];
    @observable pending = false;
    @observable err = null;
    @observable currentPage = 0;
    @observable hasMore = true;
    @computed
    get data() {
        return this.hrefs.map(x => this.bunker.getItem(x)).filter(x => x !== undefined);
    }
    constructor(url, pageSize, bunker, urlProvider = paginateUrl) {
        this.pageSize = pageSize;
        this.url = url;
        this.urlProvider = urlProvider;
        this.pagedUrl = this.urlProvider(url);
        this.pageSize = pageSize;
        this.bunker = bunker;
        this.fetchNextPage();
    }


    @action
    fetchNextPage() {
        const url = this.pagedUrl(this.currentPage * this.pageSize, this.pageSize + 1);
        
       
        this.pending = true;
        return Fetch.fetchJSON(url) // Fetch data
            .then(action('set data', data => {
                this.bunker.setItems(data);
   
                this.hrefs = this.hrefs.concat(data.slice(0, this.pageSize).map(x => x._links.self.href));
               // this.data = this.data.concat(data.slice(0, this.pageSize));
                this.hasMore = data.length > this.pageSize;
                this.currentPage = this.currentPage + 1;
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                this.err = err;
            });
    }

    @action
    refetchHrefs() {
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
}
