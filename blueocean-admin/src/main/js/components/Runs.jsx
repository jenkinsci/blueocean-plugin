import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import ModalView from 'react-header-modal';
require('moment-duration-format');

/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
    constructor(props) {
        super(props);
        this.state = {isVisible: false};
    }
    render() {
        const { data, changeset } = this.props;
        // early out
        if (!data && data.toJS) {
            return null;
        }
        let
            duration = moment.duration(
                Number(data.durationInMillis), 'milliseconds').format('hh:mm:ss');

        const
            durationArray = duration.split(':'),
            name = decodeURIComponent(data.pipeline)
        ;

        if (durationArray.length === 1) {
            duration = `00:${duration}`;
        }

        return (<tr key={data.id}>
            <td>
                {
                    this.state.isVisible && <ModalView hideOnOverlayClicked
                                                       title={`Branch ${name}`}
                                                       body={JSON.stringify(data)}
                                                       isVisible={this.state.isVisible}
                                                       afterClose={() => this.setState({isVisible: false})}/>
                }
                <a onClick={() => this.setState({isVisible: true})}>
                    {data.result}
                </a>
            </td>
            <td>{data.id}</td>
            <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8)  || '-'}</td>
            <td>{name}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td>
                {duration} minutes
            </td>
            <td>{moment(data.endTime).fromNow()}</td>
        </tr>);
    }
}

Runs.propTypes = {
    data: PropTypes.object.isRequired,
    changeset: PropTypes.object.isRequired,
};
