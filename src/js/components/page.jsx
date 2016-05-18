// @flow

import React, {Component} from 'react';

export class Page extends Component {
    render() {
        return <div id="outer">{this.props.children}</div>
    }
}
