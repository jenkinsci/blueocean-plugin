/*eslint-disable no-unused-vars*/
import React, { Component, PropTypes } from 'react';
/*eslint-enable no-unused-vars*/

class Body extends Component {
    render() {
        const {
            props: {
                children,
            },
        } = this;
        return children;
    }
}

Body.propTypes = {
    children: PropTypes.node,
};

export default Body;
