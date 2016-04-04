import React, { Component, PropTypes } from 'react';
import Immutable from 'immutable';

function placeholder() {
    return null;
}

// FIXME: We should rename this to something clearer and
// lose the capital A if we're going to keep it.
export default function ajaxHoc(ComposedComponent, getURLFromProps = placeholder) {
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
                this.fetchPipelineData(data => {
                    // eslint-disable-next-line
                    this.setState({
                        data: Immutable.fromJS(data),
                    });
                });
            }
        }

        checkUrl(props) {
            const config = this.context.config;
            const url = getURLFromProps(props, config);
            this.setState({ url }, () => this.maybeLoad());
        }

        /** FIXME: Ghetto ajax loading of pipeline data for an org @store*/
        fetchPipelineData(onLoad) {
            const xmlhttp = new XMLHttpRequest();
            const url = this.state.url;

            if (!url) {
                onLoad(null);
                return;
            }

            xmlhttp.onreadystatechange = () => {
                if (xmlhttp.readyState === XMLHttpRequest.DONE) {
                    if (xmlhttp.status === 200) {
                        let data = null;
                        try {
                            data = JSON.parse(xmlhttp.responseText);
                        } catch (e) {
                            // eslint-disable-next-line
                            console.log('Loading', url,
                                'Expecting JSON, instead got', xmlhttp.responseText);
                        }
                        onLoad(data);
                    } else {
                        // eslint-disable-next-line
                        console.log('something else other than 200 was returned');
                    }
                }
            };
            xmlhttp.open('GET', url, true);
            xmlhttp.send();
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
