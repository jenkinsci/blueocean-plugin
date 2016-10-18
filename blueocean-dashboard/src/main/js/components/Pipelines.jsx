import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { documentTitle } from './DocumentTitle';
import { observer } from 'mobx-react';

@observer
export class Pipelines extends Component {

    componentWillMount() {
        this.context.pipelinesService.fetchPipelines(this.props.params.organization);
    }

    componentDidMount() {
        // TODO: re-enable this
        //const { organization = 'Jenkins' } = this.context.params;
        //this.props.setTitle(organization);
    }

    componentWillReceiveProps(nextProps) {
        this.context.pipelinesService.fetchPipelines(nextProps.params.organization);
    }

    render() {
        // TODO: should just be able to reference pipelineService.pipelineList here
        const pipelines1 = this.context.pipelinesService._organizationList;
        const pipelines2 = this.context.pipelinesService._allPipelines;
        const pipelines = this.props.params.organization ? pipelines1 : pipelines2;

        const { config } = this.context;
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
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <a target="_blank" className="btn-secondary inverse" href={newJobUrl}>
                                New Pipeline
                            </a>
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
    pipelinesService: object,
};

Pipelines.propTypes = {
    setTitle: func,
};

export default Pipelines;
