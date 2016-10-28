import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { translate } from 'react-i18next';
import { documentTitle } from './DocumentTitle';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';
import compose from '../util/compose';

import I18nWrapper from '../util/i18n';

export class Pipelines extends Component {

    componentDidMount() {
        const { organization = 'Jenkins' } = this.context.params;
        this.props.setTitle(organization);
    }

    render() {
        const { pipelines, config, params: { organization }, location: { query } } = this.context;
        // this.i18n = new I18nWrapper('jenkins.plugins.blueocean.dashboard.Messages');
        // this.i18n.on('i18nChanged', (date) => {
        //    console.log('xxx - i18nChanged', date);
        // });
        // this.i18n.t && console.log('xxxx', this.i18n.t('Name'))
        const { t } = new I18nWrapper('jenkins.plugins.blueocean.dashboard.Messages');
        console.log('ttt', t('home.pipelineslist.header.name'));
        const orgLink = organization ?
            <Link
              to={`organizations/${organization}`}
              className="inverse"
              query={query}
            >
                {organization}
            </Link> : '';

        const headers = [
            { label: t('home.pipelineslist.header.name'), className: 'name-col' },
            t('home.pipelineslist.header.health'),
            t('home.pipelineslist.header.branches'),
            t('home.pipelineslist.header.pullrequests'),
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
                            <Link
                              to="/"
                              query={query}
                              className="inverse"
                            >
                                { t('home.header.dashboard') }
                            </Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <a target="_blank" className="btn-secondary inverse" href={newJobUrl}>
                                { t('home.header.button.createpipeline') }
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
                                          t={t}
                                          key={key} pipeline={pipeline}
                                          showOrganization={!organization}
                                        />
                                    );
                                })
                            }
                        </Table>

                        { pipelines && pipelines.$pager &&
                            <button disabled={!pipelines.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => pipelines.$pager.fetchMore()}>
                                {pipelines.$pending ? t('Loading') : t('More')}
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
    location: object.isRequired, // From react-router
};

Pipelines.propTypes = {
    setTitle: func,
    t: func,
};

const composed  = compose(
  translate(['jenkins.plugins.blueocean.dashboard.Messages'], { wait: true }),
  documentTitle
);

export default composed(Pipelines);
