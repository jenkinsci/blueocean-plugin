import React, {Component} from 'react';
import {Link} from 'react-router';

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
        const base = this.props.base;
        return (
            <nav className="page-tabs">
                {React.Children.map(this.props.children, child => React.cloneElement(child, {base}))}
            </nav>
        );
    }
}

PageTabs.propTypes = {
    base: React.PropTypes.string
};

export class TabLink extends Component {
    render() {
        const base = this.props.base || "";
        const routeUrl = base + this.props.to;
        const linkClassName = this.context.router.isActive(routeUrl) ? "selected" : undefined;
        return <Link to={routeUrl} className={linkClassName}>{this.props.children}</Link>;
    }
}

TabLink.propTypes = {
    base: React.PropTypes.string,
    to: React.PropTypes.string.isRequired
};

TabLink.contextTypes = {
    router: React.PropTypes.object
};