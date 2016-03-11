import React, {Component} from 'react';

export class PageHeader extends Component {
    render() {
        return <header className="sub-header">{this.props.children}</header>;
    }
}

export class Title extends Component {
    render() {
        // If we've been given a plain string, wrap it as <h1>
        var contents = this.props.children;
        if (typeof contents === "string") {
            contents = <h1>contents</h1>;
        }

        return <nav className="page-title">{contents}</nav>;
    }
}

export class PageTabs extends Component {
    render() {
        return <nav className="page-tabs">{this.props.children}</nav>;
    }
}
