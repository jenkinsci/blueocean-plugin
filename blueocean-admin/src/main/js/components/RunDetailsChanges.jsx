import React, { Component, PropTypes } from 'react';
import { CommitHash } from '@jenkins-cd/design-language';
import Table from './Table';

const { array } = PropTypes;

export default class RunDetailsChanges extends Component {
    render() {
        const { runs } = this.props;
        const { changeSet } = runs[0];

        const headers = [
            'Commit',
            { label: 'Author', className: 'author' },
            { label: 'Message', className: 'message' },
        ];

        return (
            <Table headers={headers} className="changeset-table">
                { changeSet.map(commit => (
                    <tr key={commit.commitId}>
                        <td><CommitHash commitId={commit.commitId} /></td>
                        <td>{commit.author.fullName}</td>
                        <td>{commit.msg}</td>
                    </tr>
                ))}
            </Table>
        );
    }
}

RunDetailsChanges.propTypes = {
    runs: array,
};
