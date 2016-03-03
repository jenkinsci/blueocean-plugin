import React, {Component} from 'react';

/**
 * This is a place holder extension to show the organisation pipeline listing. 
 * It loads the data once, lists it etc. 
 * Have at it!
 */
export default class OrganisationPipelines extends Component {  
     constructor() {
       super();
       this.state = {pipelines : []};
     }  
     componentDidMount() {
       fetchPipelineData((data) => {
         this.setState({pipelines: data});
       });              
     }
    
    render() {        
        return <div>
                  <h1><p>This is also from a plugin. Number of pipelines: {this.state.pipelines.length} </p></h1>
                    <iframe width="420" height="315" src="//www.youtube.com/embed/xZuQz1tO8aQ"></iframe>  
                    <p/>
                    {this.state.pipelines.map(renderHomepagePipeline)}                    
                </div>;
    }
}

/** Pipeline row component */
function renderHomepagePipeline(pipeline) {
    return <div key={pipeline.name}>
              <h3>{pipeline.name}</h3>
           </div>
}



/** Ghetto ajax loading of pipeline data for an org */
function fetchPipelineData(onLoad) {    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == XMLHttpRequest.DONE ) {
           if(xmlhttp.status == 200){
               console.log(xmlhttp.responseText);
               var pipes = JSON.parse(xmlhttp.responseText);
               onLoad(pipes);
               console.log(pipes);
           }  else {         
              console.log('something else other than 200 was returned')
           }
        }
    }
    xmlhttp.open("GET", "/jenkins/blue/rest/organizations/jenkins/pipelines/", true);
    xmlhttp.send();
}
