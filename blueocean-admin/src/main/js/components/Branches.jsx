import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { WeatherIcon, StatusIndicator } from '@jenkins-cd/design-language';

const { object } = PropTypes;

export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const { data } = this.props;
        // early out
        if (!data) {
            return null;
        }
        const { latestRun, weatherScore, name } = data;
        const { result, endTime, changeSet, state } = latestRun;
        const { commitId, msg } = changeSet[0] || {};
        return (<tr key={name}>
            <td><WeatherIcon score={weatherScore} /></td>
            <td>
                <StatusIndicator result={result === 'UNKNOWN' ? state : result} />
            </td>
            <td>{decodeURIComponent(name)}</td>
            <td>{commitId || '-'}</td>
            <td>{msg || '-'}</td>
            <td>{moment(endTime).fromNow()}</td>
        </tr>);
    }
}

Branches.propTypes = {
    data: object.isRequired,
};

