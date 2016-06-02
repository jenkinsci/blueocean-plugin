// @flow

import React, { Component, PropTypes } from 'react';
import fetchData from './fetcher';

export {fetchData};

function placeholder() {
    return null;
}

// FIXME: we're overloading the ES5 fetch global with this
// FIXME: config is :any because this function should never have been in the JDL.
export function fetch(ComposedComponent: ReactClass,
                      getURLFromProps: (props: any, config: any) => ?string = placeholder, toJson: boolean) {
    
    class Wrapped extends Component {

        _lastUrl: ?string;

        state: {
            data: any,
            url: ?string
        };

        constructor(props: any) {
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

        componentWillReceiveProps(nextProps: any) {
            this.checkUrl(nextProps);
        }

        maybeLoad() {
            const { url } = this.state;
            if (url && url !== this._lastUrl) {
                this._lastUrl = url;
                fetchData(data => {
                    this.setState({ data });
                }, url, toJson);
            }
        }

        checkUrl(props: any) {
            const config: any = this.context.config;
            if (config) {
                const url = getURLFromProps(props, config);
                this.setState({ url }, () => this.maybeLoad());
            }
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
