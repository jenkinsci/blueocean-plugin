class PipelineViewStore {
    constructor() {
        this.listeners = [];
        
        this.pipelines = [
             {name:"Alpha", status:"green"},
             {name:"Bravo", status:"green"},
             {name:"Charlie", status:"red"},
             {name:"Spaz", status:"green"}
         ];
    }
    
    registerListener(fn) {
        this.listeners.push(fn);
    }
    
    _notifyListeners() {
        this.listeners.forEach(l => {
            l(status);
        })
    }
    
    getPipelines() {
        return this.pipelines;
    }
    
    addPipeline(pipeline) {
        this.pipelines.push(pipeline);
        this._notifyListeners();
    }
}

export default new PipelineViewStore();
