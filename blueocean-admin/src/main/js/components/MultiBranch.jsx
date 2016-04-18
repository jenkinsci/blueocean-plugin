import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import Table from './Table';
import ajaxHoc from '../AjaxHoc';
import Branches from './Branches';
import { RunsRecord } from './records';
import { urlPrefix } from '../config';

const { object, array } = PropTypes;

export class MultiBranch extends Component {
    render() {
        const { pipeline, data } = this.props;
        // early out
        if (!data || !pipeline) {
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
export default ajaxHoc(MultiBranch, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
});
