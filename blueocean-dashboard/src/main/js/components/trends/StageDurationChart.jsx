import React, { PropTypes } from 'react';
import { Area, AreaChart, CartesianGrid, Legend, Tooltip, XAxis, YAxis } from 'recharts';


const seriesColors = [
    '#4A90E2',
    '#d54c53',
    '#78b037',
    '#F5A623',
    '#bd0fe1',
    '#24B0D5',
    '#949393',
    '#8CC04F',
    '#F6B44B',
];

function sortRowsById(row1, row2) {
    return parseInt(row1.id) - parseInt(row2.id);
}

function createChartData(rows) {
    if (!rows) {
        return [];
    }

    const mappedRows = rows.map(row => {
        const transformed = {
            id: row.id,
        };

        for (const prop of Object.keys(row.nodes)) {
            if (prop !== 'id') {
                transformed[prop] = Math.round(parseInt(row.nodes[prop]) / 1000);
            }
        }

        return transformed;
    });

    return mappedRows.sort(sortRowsById);
}

function createChartSeries(trend, rows) {
    if (!trend || !rows) {
        return [];
    }

    const columns = [];

    rows.forEach(row => {
        for (const prop of Object.keys(row.nodes)) {
            if (prop !== 'id' && columns.indexOf(prop) === -1) {
                columns.push(prop);
            }
        }
    });

    const series = [];
    const colors = seriesColors.slice();

    // create Line for each element using color from list
    for (const col of columns) {
        if (col !== 'id') {
            const color = colors.shift() || '#4A4A4A';
            series.push(
                <Area type="monotone" dataKey={col} stroke={color} fill={color} stackId="1" />
            );
        }
    }

    return series;
}


class StageDurationChart extends React.Component {
    render() {
        const rows = createChartData(this.props.rows);
        const series = createChartSeries(this.props.trend, this.props.rows);

        return (
            <AreaChart width={375} height={375} data={rows}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="id" />
                <YAxis />
                {series}
                <Legend />
                <Tooltip />
            </AreaChart>
        );
    }
}

StageDurationChart.propTypes = {
    trend: PropTypes.object,
    rows: PropTypes.object,
};


export default {
    trendId: 'stageDuration',
    componentClass: StageDurationChart,
};
