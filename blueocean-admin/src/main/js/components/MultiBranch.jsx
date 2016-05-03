import React, { Component, PropTypes } from 'react';
import Table from './Table';
import { EmptyStateView, fetch } from '@jenkins-cd/design-language';
import Branches from './Branches';
import { RunsRecord } from './records';

const { object, array } = PropTypes;

export class MultiBranch extends Component {

    renderEmptyState(repoName) {
        return (
            <main>
                <EmptyStateView iconName="shoes">
                    <h1>Branch out</h1>

                    <p>
                        Create a branch in the repository <em>{repoName}</em> and
                        Jenkins will start testing your changes.
                    </p>

                    <p>
                        Give it a try and become a hero to your team.
                    </p>

                    <button>Enable</button>
                </EmptyStateView>
            </main>
        );
    }

    render() {
        const { pipeline, data } = this.props;

        // render empty view while data loads
        if (!pipeline || !data) {
            return null;
        }

        if (!data.length) {
            return this.renderEmptyState(pipeline.name);
        }

        const headers = [
            'Health',
            'Status',
            { label: 'Branch', className: 'branch' },
            { label: 'Last commit', className: 'lastcommit' },
            { label: 'Latest message', className: 'message' },
            { label: 'Completed', className: 'completed' },
        ];

        return (
            <main>
                <article>
                    <Table className="multibranch-table"
                      headers={headers}
                    >
                        {data.map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<Branches
                              key={index}
                              data={result}
                            />);
                        })
                        }
                    </Table>
                </article>
            </main>
        );
    }
}

MultiBranch.propTypes = {
    pipeline: object,
    data: array,
};

// Decorated for ajax as well as getting pipeline from context
export default fetch(MultiBranch, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
});
