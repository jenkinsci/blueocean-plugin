import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import ajaxHoc from '../AjaxHoc';
import { ModalView, ModalBody } from '@jenkins-cd/design-language';

const { bool, object, string } = PropTypes;

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
        const { result, changeset, baseUrl, multiBranch = false } = this.props;
        // early out
        if (!result && result.toJS) {
            return null;
        }
        let
            duration = moment.duration(
                Number(result.durationInMillis), 'milliseconds').format('hh:mm:ss');

        const durationArray = duration.split(':');
        const name = decodeURIComponent(result.pipeline);

        if (durationArray.length === 1) {
            duration = `00:${duration}`;
        }

        const afterClose = () => this.setState({ isVisible: false });
        const open = () => this.setState({ isVisible: true });
        return (<tr key={result.id}>
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
                                <dd>{result.result}</dd>
                                <dt>Build</dt>
                                <dd>{result.id}</dd>
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
                                <dd>{moment(result.endTime).fromNow()}</dd>
                            </dl>
                        </ModalBody>
                    </ModalView>
                }
                <a onClick={open}>
                    {result.result}
                </a>
            </td>
            <td>{result.id}</td>
            <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8) || '-'}</td>
            <td>{name}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td>
                {duration} minutes
            </td>
            <td>{moment(result.endTime).fromNow()}</td>
        </tr>);
    }
}

Runs.propTypes = {
    result: object.isRequired,
    changeset: object.isRequired,
    baseUrl: string.isRequired,
    multiBranch: bool,
};
// Decorated for ajax
export default ajaxHoc(Runs, ({baseUrl, multiBranch, result}, config) => {
    console.log('¿?', baseUrl, multiBranch, result)
    if(multiBranch) {
        console.log(`${baseUrl}/branches/${result.pipeline}/runs/${result.id}/log/`)
    } else {
        console.log(`${baseUrl}/runs/${result.id}/log/`)
    }
    return `${baseUrl}/runs`;
});
