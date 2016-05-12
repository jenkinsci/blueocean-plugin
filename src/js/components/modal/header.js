// @flow

import React, { Component, PropTypes } from 'react';

class Header extends Component {
    render() {
        const {
            props: {
                children,
                title = 'no title',
                titleStyle
                },
            } = this;
        if (children) {
            return children;
        } else {
            return (
                <h2 className="title" style={titleStyle}>{title}</h2>
            );
        }

    }
}

Header.propTypes = {
    children: PropTypes.node,
    title: PropTypes.string,
    titleStyle: PropTypes.object,
};

export default Header;
