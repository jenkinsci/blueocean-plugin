import React, {Component} from 'react';

/** My bad extension to show handling of failures */
export default class MyBadExtension extends Component {
    
      render() {
          console.log(this.props.pipeline);
          if (this.props.pipeline.status !== "green") { // oh dear, there is no shizzle or nizzle
            return ( 
              <div>
                  <a href="#">logs</a>
              </div>
            )
          } else {
            return null;
          }
      }   
    
}
