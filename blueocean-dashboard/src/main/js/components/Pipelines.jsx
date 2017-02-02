import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';
import { documentTitle } from './DocumentTitle';

import { observer } from 'mobx-react';

const translate = i18nTranslator('blueocean-dashboard');


@observer
export class Pipelines extends Component {
    componentWillMount() {
        this._initPager(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initPager(nextProps);
    }

    _initPager(props) {
        const org = props.params.organization;
        if (org) {
            this.pager = this.context.pipelineService.organiztionPipelinesPager(org);
        } else {
            this.pager = this.context.pipelineService.allPipelinesPager();
        }
    }

    render() {
        const pipelines = this.pager.data;
        const { organization = 'Jenkins', location = {} } = this.context.params;

        if (!pipelines || this.pager.pending) {
            this.props.setTitle(translate('common.pager.loading', { defaultValue: 'Loading...' }));
        } else {
            this.props.setTitle(organization);
        }

        const orgLink = organization ?
            <Link
              to={`organizations/${organization}`}
              query={location.query}
            >
                {organization}
            </Link> : '';

        const headers = [
            { label: translate('home.pipelineslist.header.name', { defaultValue: 'Name' }), className: 'name-col' },
            translate('home.pipelineslist.header.health', { defaultValue: 'Health' }),
            translate('home.pipelineslist.header.branches', { defaultValue: 'Branches' }),
            translate('home.pipelineslist.header.pullrequests', { defaultValue: 'PR' }),
            { label: '', className: 'actions-col' },
        ];
        return (
            <Page>
                <PageHeader>
                    {!pipelines || this.pager.pending && <PageLoading duration={2000} />}
                    <Title>
                        <h1>
                            <Link
                              to="/"
                              query={location.query}
                            >
                                { translate('home.header.dashboard', { defaultValue: 'Dashboard' }) }
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
                          className="pipelines-table"
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

                        { pipelines &&
                            <button disabled={!this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                                {this.pager.pending ? translate('common.pager.loading', { defaultValue: 'Loading...' }) : translate('common.pager.more', { defaultValue: 'Show more' })}
                            </button>
                        }
                    </article>
                </main>
            </Page>);
    }
}

const { func, object } = PropTypes;

Pipelines.contextTypes = {
    config: object,
    params: object,
    store: object,
    router: object,
    pipelineService: object,
    location: object.isRequired, // From react-router
};

Pipelines.propTypes = {
    setTitle: func,
};

export default documentTitle(Pipelines);
