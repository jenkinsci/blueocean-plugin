import React, { Component, PropTypes } from 'react';
import AjaxHoc from '../AjaxHoc';
import Table from './Table';
import Runs from './Runs';
import { Link } from 'react-router';
import { urlPrefix } from '../config';

import { ActivityRecord, ChangeSetRecord } from './records';

import { Page, PageHeader, Title, WeatherIcon } from '@jenkins-cd/design-language';

import pipelinePropProvider from './pipelinePropProvider';

export class Activity extends Component {
    render() {
        const { pipeline, data } = this.props;

        if (!data || !pipeline) {
            return null;
        }
        const
          {
          name,
          weatherScore,
        } = pipeline;
        const headers = ['Status', 'Build', 'Commit', 'Branch', 'Message', 'Duration', 'Completed'];

        let latestRecord = {};

        return (<Page>

            <PageHeader>
                <Title><WeatherIcon score={weatherScore} /> <h1>CloudBees / {name}</h1></Title>
            </PageHeader>

            <main>
                <article>
                    <Table headers={headers}>
                        { data.map((run, index) => {
                            let
                            changeset = run.get('changeSet');
                            if (changeset.size > 0) {
                                changeset = changeset.toJS();
                                latestRecord = new ChangeSetRecord(
                                    changeset[Object.keys(changeset)[0]]);
                            }
                            return (<Runs
                              key={index}
                              changeset={latestRecord}
                              data={new ActivityRecord(run)}
                            />);
                        })}

                        <tr>
                            <td colSpan={headers.length}>
                                <Link className="btn" to={urlPrefix}>Dashboard</Link>
                            </td>
                        </tr>
                    </Table>
                </article>
            </main>
          </Page>);
    }
}

Activity.propTypes = {
    pipeline: PropTypes.object,
    data: PropTypes.object,
};

// Decorated for ajax as well as getting pipeline from context
export default pipelinePropProvider(AjaxHoc(Activity,(props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/runs`;
}));
