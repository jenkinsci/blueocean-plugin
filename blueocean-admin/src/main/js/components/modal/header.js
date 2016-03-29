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
                <h2 style={titleStyle} className="title">{title}</h2>
            );
        }

    }
}

Header.propTypes = {
    children: PropTypes.node,
    titleStyle: PropTypes.object,
    title: PropTypes.string,
};

export default Header;
