import React, { Component } from 'react';
import Immutable from 'immutable';

function placeholder(props) {
    return null;
}

// FIXME: We should rename this to something clearer and lose the capital A if we're going to keep it.
export default function AjaxHoc(ComposedComponent, getURLFromProps = placeholder) {

    // Keep track of the last used URL, so we won't re-load it
    let lastURL = null;

    return class extends Component {
        constructor(props) {
            super(props);

            const data = null;
            const url = getURLFromProps(props);

            this.state = {data, url};
        }

        componentWillReceiveProps(nextProps) {
            const url = getURLFromProps(nextProps);
            this.setState({url}, () => this.maybeLoad());
        }

        componentDidMount() {
            this.maybeLoad();
        }

        maybeLoad() {
            const {url} = this.state;

            if (url && url != lastURL) {
                lastURL = url;
                this.fetchPipelineData(data => {
                    // eslint-disable-next-line
                    this.setState({
                        data: Immutable.fromJS(data),
                    });
                });
            }
        }

        render() {
            return <ComposedComponent {...this.props} data={ this.state.data }/>;
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
    };
}
