import React, { Component, PropTypes } from 'react';
import Pipeline from './Pipeline';
import { components } from '@jenkins-cd/design-language';
const { Table } = components;

export default class Pipelines extends Component {

    render() {
        const { pipelines } = this.props;
        // Early out
        if (!pipelines) {
            return null;
        }
        const multiBranch = pipelines.filter(pipeline => { // eslint-disable-line arrow-body-style
            return !!pipeline.branchNames;
        });
        const noMultiBranch = pipelines.filter(pipeline => { // eslint-disable-line arrow-body-style
            return !pipeline.branchNames;
        });
        return (
            <Table className="multiBranch">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Status</th>
                    <th>Branches</th>
                    <th>Pull Requests</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                { multiBranch.map(
                    (pipeline, index) =>
                        <Pipeline key={index} pipeline={pipeline} />
                )}
                { noMultiBranch.map(
                    (pipeline, index) =>
                        <Pipeline key={index}
                          simple={true} // eslint-disable-line react/jsx-boolean-value
                          pipeline={pipeline}
                        />)}
                </tbody>
            </Table>);
    }
}

Pipelines.propTypes = {
    pipelines: PropTypes.array.isRequired,
};
