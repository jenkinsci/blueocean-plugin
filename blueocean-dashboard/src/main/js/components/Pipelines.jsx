import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import { I18n } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import CreatePipelineLink from './CreatePipelineLink';
import { documentTitle } from './DocumentTitle';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

const translate = I18n.getFixedT(I18n.language, 'jenkins.plugins.blueocean.dashboard.Messages');

export class Pipelines extends Component {

    componentDidMount() {
        const { organization = 'Jenkins' } = this.context.params;
        this.props.setTitle(organization);
    }

    render() {
        const { config, params: { organization }, location: { query } } = this.context;
        const { pipelines } = this.props;
        const orgLink = organization ?
            <Link
              to={`organizations/${organization}`}
              className="inverse"
              query={query}
            >
                {organization}
            </Link> : '';

        const headers = [
            { label: translate('home.pipelineslist.header.name'), className: 'name-col' },
            translate('home.pipelineslist.header.health'),
            translate('home.pipelineslist.header.branches'),
            translate('home.pipelineslist.header.pullrequests'),
            { label: '', className: 'actions-col' },
        ];
        return (
            <Page>
                <PageHeader>
                    {!pipelines || pipelines.$pending && <PageLoading duration={2000} />}
                    <Title>
                        <h1>
                            <Link
                              to="/"
                              query={query}
                              className="inverse"
                            >
                                { translate('home.header.dashboard') }
                            </Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <CreatePipelineLink />
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
                                          t={translate}
                                          key={key} pipeline={pipeline}
                                          showOrganization={!organization}
                                        />
                                    );
                                })
                            }
                        </Table>

                        { pipelines && pipelines.$pager &&
                            <button disabled={!pipelines.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => pipelines.$pager.fetchMore()}>
                                {pipelines.$pending ? translate('common.pager.loading') : translate('common.pager.more')}
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
    store: object,
    router: object,
    location: object.isRequired, // From react-router
};

Pipelines.propTypes = {
    setTitle: func,
    pipelines: array,
};

export default documentTitle(Pipelines);
