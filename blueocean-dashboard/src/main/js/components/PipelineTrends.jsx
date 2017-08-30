import React, { Component, PropTypes } from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from 'recharts';
import { capable, AppConfig, Fetch } from '@jenkins-cd/blueocean-core-js';

import { buildPipelineUrl } from '../util/UrlUtils';
import { ColumnFilter } from './ColumnFilter';


export const MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';

function sortRowsById(rawRows) {
    const rows = (rawRows || []).slice();
    rows.sort((row1, row2) => parseInt(row1.id) - parseInt(row2.id));
    return rows;
}

function formatJunitRows(rawRows) {
    const rows = sortRowsById(rawRows);
    return rows.filter(row => !!row.total);
}

function formatCoverageRows(rawRows) {
    const rows = sortRowsById(rawRows);
    return rows.filter(row => !!row.total);
}

function createJunitSeries() {
    return [
        <Line type="monotone" dataKey="total" stroke="#4A90E2" />,
        <Line type="monotone" dataKey="passed" stroke="#78b037" />,
        <Line type="monotone" dataKey="failed" stroke="#d54c53" />,
        <Line type="monotone" dataKey="skipped" stroke="#4A4A4A" />,
    ];
}

function createCoverageSeries() {
    return [
        <Line type="monotone" dataKey="branches" stroke="#4A90E2" />,
        <Line type="monotone" dataKey="lines" stroke="#78b037" />,
        <Line type="monotone" dataKey="methods" stroke="#F5A623" />,
        <Line type="monotone" dataKey="classes" stroke="#d54c53" />,
    ];
}

const formatterFuncs = {
    junit: formatJunitRows,
    coverage: sortRowsById,
};

const seriesFuncs = {
    junit: createJunitSeries,
    coverage: createCoverageSeries,
};


@observer
export class PipelineTrends extends Component {

    componentWillMount() {
        this.fetchTrendsData(this.props);
    }

    componentWillReceiveProps(newProps) {
        if (this.props.params && this._branchFromProps(this.props) !== this._branchFromProps(newProps)) {
            this.fetchTrendsData(newProps);
        }
    }

    @observable trends = [];

    fetchTrendsData(theProps) {
        const { pipeline } = theProps;
        const baseUrl = `${AppConfig.getRestRoot()}/organizations/${AppConfig.getOrganizationName()}/pipelines/${pipeline.fullName}`;

        let fullUrl;

        if (capable(pipeline, MULTIBRANCH_PIPELINE)) {
            const branchName = this._selectedBranch(theProps, pipeline);
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

    _branchFromProps(props) {
        return ((props.location || {}).query || {}).branch;
    }

    _selectedBranch(theProps, pipeline) {
        return this._branchFromProps(theProps) || pipeline.branchNames[0];
    }

    navigateToBranch = branch => {
        const organization = this.props.params.organization;
        const pipeline = this.props.params.pipeline;
        const baseUrl = buildPipelineUrl(organization, pipeline);
        let activitiesURL = `${baseUrl}/trends`;
        if (branch) {
            activitiesURL += '?branch=' + encodeURIComponent(branch);
        }
        this.context.router.push(activitiesURL);
    };

    render() {
        const { pipeline } = this.props;
        const trends = this.trends.slice();

        let branchFilter = null;

        if (capable(pipeline, MULTIBRANCH_PIPELINE)) {
            const branchName = decodeURIComponent(this._selectedBranch(this.props, pipeline));

            branchFilter = (
                <ColumnFilter
                    value={branchName}
                    onChange={this.navigateToBranch}
                    options={pipeline.branchNames.map(b => decodeURIComponent(b))}
                />
            );
        }

        /*
        for testing layout of many trend charts
        if (this.trends && this.trends.length) {
            for (let index = 0; index < 5; index++) {
                trends.push(this.trends[0]);
            }
        }
        */

        return (
            <div className="trends-view">
                <div className="trends-branch-filter">
                    {branchFilter}
                </div>

                <div className="trends-table">
                { trends.map(trend => {
                    const formatterFunc = formatterFuncs[trend.id] || function noFormat(row) { return row; };
                    const seriesFunc = seriesFuncs[trend.id] || function empty() { return []; };

                    const rows = formatterFunc(trend.rows);
                    const series = seriesFunc();

                    return (
                        <div className="trends-chart-container" data-trend-id={trend.id}>
                            <div className="trends-chart-label">{trend.id}</div>

                            <LineChart width={400} height={400} data={rows}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="id" />
                                <YAxis />
                                {series}
                                <Legend />
                                <Tooltip />
                            </LineChart>
                        </div>
                    );
                })}
                </div>
            </div>
        );
    }
}

PipelineTrends.propTypes = {
    params: PropTypes.object,
    location: PropTypes.object,
    pipeline: PropTypes.object,
};

PipelineTrends.contextTypes = {
    router: PropTypes.object,
};

export default PipelineTrends;
