import React, {Component} from 'react';

/** My bad extension to show handling of failures */
export default class FailureLogLink extends Component {
    
      render() {
          console.log(this.props.pipeline);
          if (this.props.pipeline.status !== "green") { 
            return ( 
              <div>
                  <a href="#">Show Log</a>
              </div>
            )
          } else {
            return null;
          }
      }   
    
}
