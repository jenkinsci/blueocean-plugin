import React, {Component} from 'react';

export class PageHeader extends Component {
    render() {
        return (
            <header className="sub-header">{this.props.children}</header>
        );
    }
}

export class Title extends Component {
    render() {
        return <nav><h1>{this.props.children}</h1></nav>
    }
}

export class PageTabs extends Component {
    render() {
        return <nav className="page-tabs">{this.props.children}</nav>;
    }
}
