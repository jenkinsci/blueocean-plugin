import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { WeatherIcon } from '@jenkins-cd/design-language';

export default class Branches extends Component {

    render() {
        const { data } = this.props;
        // early out
        if (!data) {
            return null;
        }
        const { latestRun, weatherScore, name } = data;
        const { result, endTime, changeSet } = latestRun;

        const { commitId, msg } = changeSet[0] || {};

        return (<tr key={name}>
            <td><WeatherIcon score={weatherScore} /></td>
            <td>{result}</td>
            <td>{decodeURIComponent(name)}</td>
            <td>{commitId || '-'}</td>
            <td>{msg || '-'}</td>
            <td>{moment(endTime).fromNow()}</td>
        </tr>);
    }
}

Branches.propTypes = {
    data: PropTypes.object.isRequired,
};
