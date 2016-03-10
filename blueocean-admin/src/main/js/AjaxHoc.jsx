import React, { Component } from 'react';
import Immutable from 'immutable';

export default (ComposedComponent, getStateFromStores = props => props) => class extends Component {
    constructor(props) {
        super(props);
        const getFetchData = getStateFromStores(props);
        this.state = {
            data: null,
            url: getFetchData.url
        };
    }

    componentDidMount() {
        this.fetchPipelineData(data => {
            this.setState({
                data: Immutable.fromJS(data)
            });
        });
    }

    render() {
        return <ComposedComponent {...this.props} data={ this.state.data } />;
    }

  /** FIXME: Ghetto ajax loading of pipeline data for an org @store*/
    fetchPipelineData(onLoad) {
        const xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = () => {
            if (xmlhttp.readyState === XMLHttpRequest.DONE) {
                if (xmlhttp.status === 200) {
                    const data = JSON.parse(xmlhttp.responseText);
                    onLoad(data);
                } else {
                    console.log('something else other than 200 was returned');
                }
            }
        };
        xmlhttp.open('GET', this.state.url, true);
        xmlhttp.send();
    }
};
