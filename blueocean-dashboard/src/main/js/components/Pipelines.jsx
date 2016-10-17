import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { translate } from 'react-i18next';
import { documentTitle } from './DocumentTitle';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

export class Pipelines extends Component {

    componentDidMount() {
        const { organization = 'Jenkins' } = this.context.params;
        this.props.setTitle(organization);
    }

    render() {
        const { pipelines, config, params: { organization } } = this.context;
        const { t } = this.props;

        const orgLink = organization ?
            <Link to={`organizations/${organization}`} className="inverse">
                {organization}
            </Link> : '';

        const headers = [
            { label: 'Name', className: 'name-col' },
            t('bo.dashboard.pipelines.table.health'),
            t('bo.dashboard.pipelines.table.branches'),
            t('bo.dashboard.pipelines.table.pr'),
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
                            <Link to="/" className="inverse">{ t('bo.dashboard.pipelines.heading') }</Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <a target="_blank" className="btn-secondary inverse" href={newJobUrl}>
                                { t('bo.dashboard.pipelines.new.pipeline') }
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
                                {pipelines.$pending ? t('bo.dashboard.loading') : t('bo.dashboard.more')}
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
    t: func,
};

export default translate(['jenkins.plugins.blueocean.dashboard.Messages'], { wait: true })(documentTitle(Pipelines));
