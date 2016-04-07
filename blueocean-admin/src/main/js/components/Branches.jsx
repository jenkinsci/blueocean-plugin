import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { WeatherIcon, ModalView, ModalBody, StatusIndicator } from '@jenkins-cd/design-language';

const {object} = PropTypes;

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
        const afterClose = () => this.setState({ isVisible: false });
        const open = () => this.setState({ isVisible: true });
        return (<tr key={name}>
            <td><WeatherIcon score={weatherScore} /></td>
            <td>
                {
                    this.state.isVisible && <ModalView hideOnOverlayClicked
                      title={`Branch ${name}`}
                      isVisible={this.state.isVisible}
                      afterClose={afterClose}
                    >
                        <ModalBody>
                            <dl>
                                <dt>Health</dt>
                                <dd><WeatherIcon score={weatherScore} /></dd>
                                <dt>Status</dt>
                                <dd>{result}</dd>
                                <dt>Branch</dt>
                                <dd>{decodeURIComponent(name)}</dd>
                                <dt>Commit</dt>
                                <dd>{commitId || '-'}</dd>
                                <dt>Message</dt>
                                <dd>{msg || '-'}</dd>
                                <dt>Completed</dt>
                                <dd>{moment(endTime).fromNow()}</dd>
                            </dl>
                        </ModalBody>
                    </ModalView>
                }
                <a onClick={open}>
                    <StatusIndicator result={result === 'UNKNOWN' ? state : result} />
                </a>
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

