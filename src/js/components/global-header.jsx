// @flow

import React, {Component} from 'react';

export class GlobalHeader extends Component {
    render() {
        return <header className="global-header">{this.props.children}</header>;
    }
}

export class GlobalNav extends Component {
    render() {
        return <nav>{this.props.children}</nav>;
    }
}
