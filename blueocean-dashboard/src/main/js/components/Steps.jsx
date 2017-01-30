import React, { Component, PropTypes } from 'react';
import Step from './Step';

export default class Nodes extends Component {
    render() {
        const { nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            return null;
        }
        const {
            model,
            nodesBaseUrl,
        } = nodeInformation;
        const { logs, fetchLog, followAlong, url, location, router, t, locale } = this.props;
        return (<div>
            {
              model.map((item, index) =>
                <Step
                  {...{
                      key: `${index}${item.id}`,
                      node: item,
                      logs,
                      nodesBaseUrl,
                      fetchLog,
                      followAlong,
                      url,
                      location,
                      router,
                      t,
                      locale,
                  }}
                />)
            }
        </div>);
    }
}

Nodes.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
    node: PropTypes.object.isRequired,
    followAlong: PropTypes.bool,
    logs: PropTypes.object,
    location: PropTypes.object,
    fetchLog: PropTypes.func,
    nodesBaseUrl: PropTypes.string,
    router: PropTypes.shape,
    url: PropTypes.string,
    locale: PropTypes.object,
    t: PropTypes.func,
};
