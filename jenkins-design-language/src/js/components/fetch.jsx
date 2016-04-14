import React, { Component, PropTypes } from 'react';
import fetchData from './fetcher';

function placeholder() {
    return null;
}

export function fetch(ComposedComponent, getURLFromProps = placeholder, toJson) {
    class Wrapped extends Component {
        constructor(props) {
            super(props);

            const data = null;
            const url = null;

            this.state = { data, url };
            this._lastUrl = null; // Keeping this out of the react state so we can set it any time
        }


        componentWillMount() {
            this.checkUrl(this.props);
        }

        componentDidMount() {
            this.maybeLoad();
        }

        componentWillReceiveProps(nextProps) {
            this.checkUrl(nextProps);
        }

        maybeLoad() {
            const { url } = this.state;
            if (url && url !== this._lastUrl) {
                this._lastUrl = url;
                fetchData(data => {
                    this.setState({ data });
                }, this.state.url, toJson);
            }
        }

        checkUrl(props) {
            const config = this.context.config;
            const url = getURLFromProps(props, config);
            this.setState({ url }, () => this.maybeLoad());
        }


        render() {
            return <ComposedComponent {...this.props} data={ this.state.data } />;
        }

    }

    Wrapped.contextTypes = {
        config: PropTypes.object,
    };

    return Wrapped;
}

export {
    fetch,
    fetchData,
}
