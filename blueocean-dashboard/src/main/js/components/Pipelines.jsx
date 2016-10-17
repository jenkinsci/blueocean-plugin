import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { documentTitle } from './DocumentTitle';

export class Pipelines extends Component {

    componentDidMount() {
        const { organization = 'Jenkins' } = this.context.params;
        this.props.setTitle(organization);
    }

    render() {
        const { pipelines } = this.context;
        const { organization } = this.context.params;

        const orgLink = organization ?
            <Link to={`organizations/${organization}`} className="inverse">
                {organization}
            </Link> : '';

        const headers = [
            { label: 'Name', className: 'name-col' },
            'Health',
            'Branches',
            'Pull Requests',
            { label: '', className: 'actions-col' },
        ];

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
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <Link to="/create-pipeline" className="btn-secondary inverse">
                                New Pipeline
                            </Link>
                        </Extensions.Renderer>
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

const { array, func, object } = PropTypes;

Pipelines.contextTypes = {
    config: object,
    params: object,
    pipelines: array,
    store: object,
    router: object,
};

Pipelines.propTypes = {
    setTitle: func,
};

export default documentTitle(Pipelines);
