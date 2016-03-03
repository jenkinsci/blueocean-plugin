
import React, { PropTypes, Component } from 'react';

export default class Spinner extends Component {

  static propTypes ={
    style: PropTypes.object
  };

  static defaultProps = {
    style: {}
  };

  render () {
    return (
      <div id='plugin-spinner' className='spinner double-bounce2'>
        <i className='icon-stopwatch' style={this.props.style}>
          {this.props.children}
        </i>
      </div>
    );
  }

};
