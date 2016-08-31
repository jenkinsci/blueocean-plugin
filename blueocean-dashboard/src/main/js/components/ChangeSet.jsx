// @flow

import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';

export default class ChangeSet extends Component {

    componentDidMount() {
        window.addEventListener('resize', () => this.condense(), true);
        this.condense();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.changeSet !== this.props.changeSet) {
            this.condense();
        }
    }

    condense() {
        const domNode = ReactDOM.findDOMNode(this.refs.authorsWrapper); // used to check for overflow
        const domNodeLink = ReactDOM.findDOMNode(this.refs.authors); // the a which need to refresh the content
        if (domNode && domNodeLink && domNode.scrollWidth > domNode.clientWidth) {
            const hint = `${this.props.changeSet.length} changes`;
            domNodeLink.textContent = hint;
        }
    }

    render() {
        const { changeSet, onClick } = this.props;
        const authors = changeSet && changeSet.map ? [...(new Set(changeSet.map(change => change.author.fullName)):any)] : [];
        return (<div ref="authorsWrapper">
            { authors.length > 0 ?
                <a ref="authors" className="authors" onClick={onClick}>
                    Changes by {authors.map(
                    author => ` ${author}`)}
                </a>
                : 'No changes' }
        </div>);
    }
}


const { array, func } = PropTypes;

ChangeSet.propTypes = {
    changeSet: array,
    onClick: func,
};
