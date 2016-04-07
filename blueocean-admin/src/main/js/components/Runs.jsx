import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { ModalView, ModalBody, StatusIndicator } from '@jenkins-cd/design-language';

require('moment-duration-format');

/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
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

        const durationArray = duration.split(':');
        const name = decodeURIComponent(data.pipeline);

        if (durationArray.length === 1) {
            duration = `00:${duration}`;
        }

        const afterClose = () => this.setState({ isVisible: false });
        const open = () => this.setState({ isVisible: true });
        const result = data.result === 'UNKNOWN' ? data.state : data.result;
        return (<tr key={data.id}>
            <td>
                {
                    this.state.isVisible && <ModalView hideOnOverlayClicked
                      title={`Branch ${name}`}
                      isVisible={this.state.isVisible}
                      afterClose={afterClose}
                    >
                        <ModalBody>
                            <dl>
                                <dt>Status</dt>
                                <dd>
                                    <StatusIndicator result={result} />
                                </dd>
                                <dt>Build</dt>
                                <dd>{data.id}</dd>
                                <dt>Commit</dt>
                                <dd>
                                    {changeset
                                        && changeset.commitId
                                        && changeset.commitId.substring(0, 8) || '-'
                                    }
                                </dd>
                                <dt>Branch</dt>
                                <dd>{name}</dd>
                                <dt>Message</dt>
                                <dd>{changeset && changeset.comment || '-'}</dd>
                                <dt>Duration</dt>
                                <dd>{duration} minutes</dd>
                                <dt>Completed</dt>
                                <dd>{moment(data.endTime).fromNow()}</dd>
                            </dl>
                        </ModalBody>
                    </ModalView>
                }
                <a onClick={open}>
                    <StatusIndicator result={result} />
                </a>
            </td>
            <td>{data.id}</td>
            <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8) || '-'}</td>
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
