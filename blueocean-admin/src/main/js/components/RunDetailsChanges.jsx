import React, { Component, PropTypes } from 'react';
import { CommitHash, EmptyStateView } from '@jenkins-cd/design-language';
import Table from './Table';

const { array } = PropTypes;

export default class RunDetailsChanges extends Component {
    renderEmptyState() {
        return (
            <EmptyStateView iconName="shoes">
                <h1>Ready, get set...</h1>

                <p>
                    Hmm, looks like there were no changesets associated with this run.
                </p>

                <p>
                    This should happen only for the first push of a branch.
                </p>
            </EmptyStateView>
        );
    }

    render() {
        const { runs } = this.props;
        const { changeSet } = runs[0];

        if (!changeSet || !changeSet.length) {
            return this.renderEmptyState();
        }

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
