// @flow
import { observable, action, computed } from 'mobx';
import { Fetch } from '../fetch';
import { DataBunker } from '../model/DataBunker';
/**
 * Provide a pagination function for the generic
 * blueocean pagination
 */
export function paginateUrl(url: string) {
    const sep = url.indexOf('?') >= 0 ? '&' : '?';
    return (start: number, limit: number) => `${url}${sep}start=${start}&limit=${limit}`;
}
type PagedUrl = (start: number, end: number) => string;
type UrlProvider = (url: string) => PagedUrl;
export class Pager {
    pageSize: number;
    url: string;
    bunker: DataBunker;
    urlProvider: UrlProvider;
    pagedUrl: PagedUrl;
    @observable hrefs = [];
    @observable pending = false;
    @observable err = null;
    @observable currentPage = 0;
    @observable hasMore = true;
   
    @computed
    get data(): Array<Object> {
        return this.hrefs.map(x => this.bunker.getItem(x)).filter(x => x !== undefined);
    }
    constructor(url: string, pageSize: number, bunker: toString, urlProvider: UrlProvider = paginateUrl) {
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
                const saved = this.bunker.setItems(data);
   
                this.hrefs = this.hrefs.concat(saved.slice(0, this.pageSize).map(x => x._links.self.href));
               // this.data = this.data.concat(data.slice(0, this.pageSize));
                this.hasMore = data.length > this.pageSize;
                this.currentPage = this.currentPage + 1;
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                action('set err', () => this.err = err);
            });
    }

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

    @action
    insert(href, pos = 0) {
        this.hrefs.splice(pos, 0, href);
    }

    has(href) {
        return this.hrefs.indexOf(href) > 0;
    }
}
