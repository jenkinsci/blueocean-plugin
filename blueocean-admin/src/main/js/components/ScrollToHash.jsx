import React, { Component } from 'react';
// This will scroll to the hash that is selected in the window.location
// React needs the timeout to have the dom ready
export const scrollToHash = ComposedComponent => class extends Component {
    componentDidMount() {
        setTimeout(() => {
            this.scrollToAnchor();
        });
    }

    scrollToAnchor() {
        const anchorName = window.location.hash;
        if (anchorName) {
            const anchorElement = document.getElementById(anchorName.replace('#', ''));
            if (anchorElement) {
                anchorElement.scrollIntoView();
            }
        }
    }

    render() {
        return <ComposedComponent {...this.props} {...this.state} />;
    }
};
