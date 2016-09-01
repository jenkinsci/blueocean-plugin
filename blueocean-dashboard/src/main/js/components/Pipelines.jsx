import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';

const { array } = PropTypes;

export default class Pipelines extends Component {

    render() {
        const { pipelines, config } = this.context;
        const { organization } = this.context.params;

        const orgLink = organization ?
            <Link to={`organizations/${organization}`} className="inverse">
                {organization}
            </Link> : '';

        const headers = [
            { label: 'Name', className: 'name' },
            'Health',
            'Branches',
            'Pull Requests',
            { label: '', className: 'favorite' },
        ];

        const baseUrl = config.getRootURL();
        const newJobUrl = `${baseUrl}/view/All/newJob`;

        return (
            <Page>
                <PageHeader>
                    {!pipelines || pipelines.$pending && <PageLoading duration={2000} />}
                    <Title>
                        <h1>
                            <Link to="/" className="inverse">Dashboard</Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                        <a target="_blank" className="btn-secondary inverse" href={newJobUrl}>
                            New Pipeline
                        </a>
                        <Link className="btn-secondary inverse" to="/pipeline-editor-demo">
                            Editor {/* TODO: Move this button into extension? */}
                        </Link>
                    </Title>
                </PageHeader>
                <main>
                    <article>
                        { /* TODO: need to adjust Extensions to make store available */ }
                        <Extensions.Renderer
                          extensionPoint="jenkins.pipeline.list.top"
                          store={this.context.store}
                          router={this.context.router}
                        />
                        <Table
                          className="pipelines-table fixed"
                          headers={headers}
                        >
                            { pipelines &&
                                pipelines.map(pipeline => {
                                    const key = pipeline._links.self.href;
                                    return (
                                        <PipelineRowItem
                                          key={key} pipeline={pipeline}
                                          showOrganization={!organization}
                                        />
                                    );
                                })
                            }
                        </Table>
                        
                        { pipelines && pipelines.$pager &&
                            <button disabled={!pipelines.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => pipelines.$pager.fetchMore()}>
                                {pipelines.$pending ? 'Loading...' : 'Show More'}
                            </button>
                        }
                    </article>
                </main>
            </Page>);
    }
}

Pipelines.contextTypes = {
    config: PropTypes.object,
    params: PropTypes.object,
    pipelines: array,
    store: PropTypes.object,
    router: PropTypes.object,
};
