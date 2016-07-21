export default class PaginationHolder {
    constructor(pageSize = 2) {
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
        this.currentData = this.currentData.slice(pageStart);
        this.currentData = this.currentData.concat(data);
    }
    
    getCurrentPageStart() {
        return this.currentPage * this.pageSize;
    }
}
