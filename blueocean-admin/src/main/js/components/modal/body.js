/*eslint-disable no-unused-vars*/
import React, { Component, PropTypes } from 'react';
/*eslint-enable no-unused-vars*/

class Body extends Component {
    render() {
        const {
            props: {
                children,
                body = 'no body'
            },
        } = this;
      if (children) {
          return children;
      } else {
          return (<span>{body}</span>);
      }
    }
}

Body.propTypes = {
    children: PropTypes.node,
    body: PropTypes.string,
};

export default Body;
