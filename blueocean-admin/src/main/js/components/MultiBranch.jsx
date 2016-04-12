import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import Table from './Table';
import ajaxHoc from '../AjaxHoc';
import Branches from './Branches';
import { RunsRecord } from './records';
import { urlPrefix } from '../config';

export class MultiBranch extends Component {
    render() {
        const { pipeline, data } = this.props;
        // early out
        if (!data || !pipeline) {
            return null;
        }

        const headers =
            ['Health', 'Status', 'Branch', 'Last commit', 'Latest message', 'Completed'];

        return (
            <main>
                <article>
                    <Table className="multiBranch"
                      headers={headers}
                    >
                        {data.map((run, index) => {
                            const result = new RunsRecord(run.toJS());
                            return <Branches key={index} data={result} />;
                        })
                        }
                        <tr>
                            <td colSpan={headers.length}>
                                <Link className="btn" to={urlPrefix}>Dashboard</Link>
                            </td>
                        </tr>
                    </Table>
                </article>
            </main>
        );
    }
}

MultiBranch.propTypes = {
    pipeline: PropTypes.object,
    data: PropTypes.object,
};

// Decorated for ajax as well as getting pipeline from context
export default ajaxHoc(MultiBranch, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
});
