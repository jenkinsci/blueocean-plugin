import React, {Component} from 'react';

/** My bad extension to show handling of failures */
export default class MyBadExtension extends Component {
    render() {        
          if (shizzle.nizzle) { // oh dear, there is no shizzle or nizzle
              return 
                <div className={'pipelineStatus_'+this.props.pipeline.status}>{this.props.pipeline.status}</div>
          }          
    }
}
