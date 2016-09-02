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
        const { props: { changeSet, onAuthorsClick }, state: { condense } } = this;
        const authors = changeSet && changeSet.map ? [...(new Set(changeSet.map(change => change.author.fullName)):any)] : [];
        let children = 'No changes';
        if (authors && authors.length > 0) {
            let nested;
            if (condense) {
                nested = `${changeSet.length} changes`;
            } else {
                nested = `Changes by ${authors.map(author => ` ${author}`)} `;
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
};
