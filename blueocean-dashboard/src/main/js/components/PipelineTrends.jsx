import React, { Component, PropTypes } from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import { CartesianGrid, LineChart, Line, XAxis, YAxis } from 'recharts';
import { capable, AppConfig, Fetch } from '@jenkins-cd/blueocean-core-js';

@observer
export class PipelineTrends extends Component {

    @observable trends = [];

    componentWillMount() {
        const { pipeline } = this.props;
        const baseUrl = `${AppConfig.getRestRoot()}/organizations/${AppConfig.getOrganizationName()}/pipelines/${pipeline.fullName}`;

        let fullUrl;

        if (capable(pipeline, 'jenkins.branch.MultiBranchProject')) {
            const branchName = pipeline.branchNames[0];
            fullUrl = `${baseUrl}/branches/${encodeURIComponent(branchName)}/trends/`;
        } else {
            fullUrl = `${baseUrl}/trends/`;
        }

        Fetch.fetchJSON(fullUrl)
            .then(data => this._updateTrends(data));
    }

    @action
    _updateTrends(data) {
        this.trends = data;
    }

    render() {
        const trends = this.trends;

        // TODO: show a branch filter?
        return (
            <div className="trends-table">

            { trends.map(trend => {
                const data = trend.rows.sort((row1, row2) => row1.id.localeCompare(row2.id));

                return (
                    <div className="trends-chart-container">
                        <div className="trends-chart-label">{trend.id}</div>

                        <LineChart width={400} height={400} data={data}>
                            <Line type="monotone" dataKey="total" stroke="#8884d8" />
                            <CartesianGrid stroke="#ccc" />
                            <XAxis dataKey="id" />
                            <YAxis />
                        </LineChart>
                    </div>
                );
            })}
            </div>
        );
    }
}

PipelineTrends.propTypes = {
    locale: PropTypes.string,
    t: PropTypes.func,
    pipeline: PropTypes.object,
    params: PropTypes.object,
};

PipelineTrends.contextTypes = {
    config: PropTypes.object.isRequired,
    params: PropTypes.object.isRequired,
    pipelineService: PropTypes.object.isRequired,
};

export default PipelineTrends;
