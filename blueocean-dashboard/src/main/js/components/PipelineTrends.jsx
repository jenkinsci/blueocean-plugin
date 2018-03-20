import React, { Component, PropTypes } from 'react';
import { action, asFlat, observable } from 'mobx';
import { observer } from 'mobx-react';
import { CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from 'recharts';
import { capable, AppConfig, Fetch } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { buildPipelineUrl } from '../util/UrlUtils';
import { ColumnFilter } from './ColumnFilter';

export const MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';

const seriesColors = ['#4A90E2', '#d54c53', '#78b037', '#F5A623', '#bd0fe1'];

function sortRowsById(row1, row2) {
    return parseInt(row1.id) - parseInt(row2.id);
}

function createChartData(rows) {
    if (!rows) {
        return [];
    }

    return rows.sort(sortRowsById);
}

function createChartSeries(trend, rows) {
    if (!trend) {
        return [];
    }

    let columns = [];

    if (trend.columns) {
        columns = Object.keys(trend.columns);
    }

    if (!columns.length && rows) {
        rows.forEach(row => {
            for (const prop of Object.keys(row)) {
                if (prop !== 'id' && columns.indexOf(prop) === -1) {
                    columns.push(prop);
                }
            }
        });
    }

    const series = [];
    const colors = seriesColors.slice();

    // create Line for each element using color from list
    for (const col of columns) {
        if (col !== 'id') {
            const color = colors.shift() || '#4A4A4A';
            series.push(<Line type="monotone" dataKey={col} stroke={color} />);
        }
    }

    return series;
}

function DefaultChart(props) {
    const series = createChartSeries(props.trend, props.rows);
    const data = createChartData(props.rows);

    return (
        <LineChart width={375} height={375} data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="id" />
            <YAxis />
            {series}
            <Legend />
            <Tooltip />
        </LineChart>
    );
}

DefaultChart.propTypes = {
    trend: PropTypes.object,
    rows: PropTypes.object,
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

    @observable extensions = asFlat({});
    @observable trends = [];
    rowsMap = {};

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

        Fetch.fetchJSON(fullUrl).then(data => this._loadTrendsSuccess(data));
    }

    _loadTrendsSuccess(trends) {
        Extensions.store.getExtensions('jenkins.pipeline.trends', extensions => this.updateTrends(trends, extensions));
    }

    @action
    updateTrends(trends, extensions) {
        extensions.forEach(trendExt => {
            this.extensions[trendExt.trendId] = trendExt.componentClass;
        });

        this.trends = trends;

        const rowsMap = {};

        for (const trend of trends) {
            const rowsUrl = trend._links && trend._links.rows && trend._links.rows.href;

            if (rowsUrl) {
                Fetch.fetchJSON(rowsUrl).then(rows => this._updateTrendRows(trend, rows));
            }

            rowsMap[trend.id] = [];
        }

        this.rowsMap = observable(rowsMap);
    }

    @action
    _updateTrendRows(trend, rows) {
        this.rowsMap[trend.id] = rows;
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

            branchFilter = <ColumnFilter value={branchName} onChange={this.navigateToBranch} options={pipeline.branchNames.map(b => decodeURIComponent(b))} />;
        }

        return (
            <div className="trends-view">
                <div className="trends-branch-filter">{branchFilter}</div>

                <div className="trends-table">
                    {trends.map(trend => {
                        const CustomComponent = this.extensions[trend.id];
                        const rows = this.rowsMap[trend.id];

                        if (!rows || !rows.length) {
                            // forces a full re-render of the chart when branch is changed, for animations
                            return null;
                        }

                        let chart = null;

                        if (CustomComponent) {
                            chart = (
                                <Extensions.SandboxedComponent>
                                    <CustomComponent trend={trend} rows={rows.slice()} />
                                </Extensions.SandboxedComponent>
                            );
                        } else {
                            chart = <DefaultChart trend={trend} rows={rows.slice()} />;
                        }

                        return (
                            <div className="trends-chart-container" data-trend-id={trend.id}>
                                <div className="trends-chart-label">{trend.displayName}</div>

                                {chart}
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
