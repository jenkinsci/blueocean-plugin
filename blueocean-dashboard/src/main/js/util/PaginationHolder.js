export default class PaginationHolder {
    constructor(pageSize = 25) {
        this.currentPage = 0;
        this.pageSize = pageSize;
        this.currentData;
        this.hasMoreData = false;
    }

    getTotalPages() {
        return this.currentData ? Math.floor(this.currentData.length / this.pageSize) : 0;
    }

    appendData(pageStart, data) {
        // TODO figure out where it goes based on pageStart
        this.currentData = this.currentData || [];
        // NOTE: sometimes this array ref leaks across page holders
        // and might be updated twice because of redux oddities.
        // defend against duplicate additions
        if (this.currentData.length <= pageStart) {
            this.currentData = this.currentData.concat(data);
        }
    }

    getCurrentPageStart() {
        return this.currentPage * this.pageSize;
    }
}
