import React, { Component, PropTypes } from 'react';
import PipelineRowItem from './PipelineRowItem';
import { PipelineRecord } from './records';
import Table from './Table';

import { Page, PageHeader, Title } from '@jenkins-cd/design-language';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';

const { array } = PropTypes;

export default class Pipelines extends Component {

    constructor(props) {
        super(props);
        this.state = {
            showAllPipelines: false,
        };
        this.onShowAllClick = this.onShowAllClick.bind(this);
    }

    onShowAllClick() {
        this.state.showAllPipelines = true;
        this.forceUpdate();
    }

    render() {
        const { pipelines, config } = this.context;
        const numInitialPiplinesToDisplay = 5;

        // Early out
        if (!pipelines) {
            return <div>No pipelines found.</div>;
        }

        const pipelineRecords = pipelines
            .map(data => new PipelineRecord(data))
            .sort(pipeline => !!pipeline.branchNames);

        // Identify if we will only display the first {numInitialPiplinesToDisplay} pipelines or display them all
        const isShowMoreButtonVisible = !this.state.showAllPipelines && pipelines.length > numInitialPiplinesToDisplay;
        var pipelinesToDisplay = [];
        if(isShowMoreButtonVisible) {
            for(var i = 0; i < pipelines.length; i++) {
                if(i >= numInitialPiplinesToDisplay) {
                    break;
                }
                pipelinesToDisplay.push(pipelines[i]);
            }
        } else {
            pipelinesToDisplay = pipelineRecords;
        }

        const headers = [
            { label: 'Name', className: 'name' },
            'Health',
            'Branches',
            'Pull Requests',
            { label: '', className: 'favorite' },
        ];


        console.log('len', pipelinesToDisplay);
        console.log('isShowMoreButtonVisible', isShowMoreButtonVisible);

        const baseUrl = config.getRootURL();
        const newJobUrl = `${baseUrl}view/All/newJob`;

        return (
            <Page>
                <PageHeader>
                    <Title>
                        <h1>Dashboard</h1>
                        <a target="_blank" className="btn-inverse" href={newJobUrl}>
                            New Pipeline
                        </a>
                    </Title>
                </PageHeader>
                <main>
                    <article>
                        <ExtensionPoint name="jenkins.pipeline.list.top" />
                        <Table
                          className="pipelines-table"
                          headers={headers}
                        >
                            { pipelinesToDisplay
                                .map(pipeline => <PipelineRowItem
                                  key={pipeline.name} pipeline={pipeline}
                                />)
                            }
                            {isShowMoreButtonVisible &&
                                <tr>
                                    <td colspan="5">
                                        <button className="pipelines-show-all-button" onClick={this.onShowAllClick}>Show All</button>
                                    </td>
                                </tr>
                            }

                        </Table>
                    </article>
                </main>
            </Page>);
    }
}

Pipelines.contextTypes = {
    pipelines: array,
    config: PropTypes.object,
};
