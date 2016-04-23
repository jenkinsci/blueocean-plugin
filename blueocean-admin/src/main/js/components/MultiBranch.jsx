import React, { Component, PropTypes } from 'react';
import Table from './Table';
import { fetch } from '@jenkins-cd/design-language';
import Branches from './Branches';
import { RunsRecord } from './records';

const { object, array } = PropTypes;

export class MultiBranch extends Component {
    render() {
        const { data } = this.props;
        // early out
        if (!data) {
            return null;
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
