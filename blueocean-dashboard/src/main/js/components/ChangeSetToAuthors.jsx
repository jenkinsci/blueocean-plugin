import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';

export default class ChangeSetToAuthors extends Component {

    constructor(props) {
        super(props);
        this.state = { condense: false };
        this.condense = this.condense.bind(this);
    }

    componentDidMount() {
        window.addEventListener('resize', this.condense, true);
        this.condense();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.changeSet !== this.props.changeSet) {
            this.condense();
        }
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.condense, true);
    }

    condense() {
        const domNode = ReactDOM.findDOMNode(this.refs.authorsWrapper); // used to check for overflow
        if (domNode && domNode.scrollWidth > domNode.clientWidth) {
            this.setState({ condense: true });
        }
    }

    render() {
        const { props: { changeSet, onAuthorsClick, t }, state: { condense } } = this;
        const authors = changeSet && changeSet.map ? [...(new Set(changeSet.map(change => change.author.fullName)):any)] : [];
        let children = t('rundetail.header.changes.none', {
            defaultValue: 'No changes',
        });
        if (authors && authors.length > 0) {
            let nested;
            if (condense) {
                nested = t('rundetail.header.changes.count', {
                    defaultValue: '{0} changes',
                    0: changeSet.length,
                });
            } else {
                nested = t('rundetail.header.changes.names', {
                    0: authors.map(author => ` ${author}`),
                    defaultValue: 'Changes by {0}',
                });
            }
            children = (<a className="authors" onClick={onAuthorsClick}>
               {nested}
            </a>);
        }
        return (<div ref="authorsWrapper">
            {children }
        </div>);
    }
}

const { array, func } = PropTypes;

ChangeSetToAuthors.propTypes = {
    changeSet: array,
    onAuthorsClick: func,
    t: func,
};
