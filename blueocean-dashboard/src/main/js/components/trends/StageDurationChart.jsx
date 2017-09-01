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

function createChartData(trend) {
    if (!trend || !trend.rows) {
        return [];
    }

    const rows = trend.rows.map(row => {
        const transformed = {
            id: row.id,
        };

        for (const prop of Object.keys(row)) {
            if (prop !== 'id') {
                transformed[prop] = Math.round(parseInt(row[prop]) / 1000);
            }
        }

        return transformed;
    });

    return rows.sort(sortRowsById);
}

function createChartSeries(trend) {
    if (!trend || !trend.rows) {
        return [];
    }

    const columns = [];

    trend.rows.forEach(row => {
        for (const prop of Object.keys(row)) {
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
        const { trend } = this.props;
        const rows = createChartData(trend);
        const series = createChartSeries(trend);

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
};


export default {
    trendId: 'stageDuration',
    componentClass: StageDurationChart,
};
