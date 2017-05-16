import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, Table, TextInput } from '@jenkins-cd/design-language';
import { i18nTranslator, ContentPageHeader, AppConfig, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';

import { documentTitle } from './DocumentTitle';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import { DashboardPlaceholder } from './placeholder/DashboardPlaceholder';

const translate = i18nTranslator('blueocean-dashboard');

@observer
export class Pipelines extends Component {
    constructor() {
        super();
        this.state = {};
    }

    onChange = debounce( e => {
        this.setState({ searchText: e });
    }, 200);

    _initPager() {
        const org = this.props.params.organization ? this.props.params.organization : AppConfig.getOrganizationName();
        const searchText = this.state.searchText;

        this.pager = this.context.pipelineService.pipelinesPager(org, searchText);
    }

    render() {
        this._initPager();

        const pipelines = this.pager.data;
        const { organization, location = { } } = this.context.params;

        const orgLink = organization ?
            <Link
                to={ `organizations/${organization}` }
                query={ location.query }
            >
                { organization }
            </Link> : '';

        const showPipelineList = pipelines && pipelines.length > 0;
        const showEmptyState = !this.pager.pending && !this.state.searchText && (!pipelines || !pipelines.length);

        const headers = [
            { label: translate('home.pipelineslist.header.name', { defaultValue: 'Name' }), className: 'name-col' },
            translate('home.pipelineslist.header.health', { defaultValue: 'Health' }),
            translate('home.pipelineslist.header.branches', { defaultValue: 'Branches' }),
            translate('home.pipelineslist.header.pullrequests', { defaultValue: 'PR' }),
            { label: '', className: 'actions-col' },
        ];
        this.props.setTitle('Jenkins Blue Ocean');

        return (
            <Page>
                <ContentPageHeader>
                    <div className="u-flex-grow">
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.header">
                            <h1>
                                <Link to="/" query={ location.query }>
                                    { translate('home.header.dashboard', { defaultValue: 'Dashboard' }) }
                                </Link>
                                { organization && ' / ' }
                                { organization && orgLink }
                            </h1>
                        </Extensions.Renderer>
                        <TextInput className="search-pipelines-input" iconLeft="search" placeholder="Search pipelines..." onChange={this.onChange} />
                    </div>
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                        <CreatePipelineLink />
                    </Extensions.Renderer>
                </ContentPageHeader>
                <main>
                    <article>
                        <Extensions.Renderer
                            extensionPoint="jenkins.pipeline.list.top"
                            store={ this.context.store }
                            router={ this.context.router }
                        />
                        { showEmptyState && <DashboardPlaceholder t={translate} /> }
                        { showPipelineList &&
                        <Table
                            className="pipelines-table"
                            headers={ headers }
                        >
                            { pipelines &&
                            pipelines.map(pipeline => {
                                const key = pipeline._links.self.href;
                                return (
                                    <PipelineRowItem
                                        t={ translate }
                                        key={ key } pipeline={ pipeline }
                                        showOrganization={ AppConfig.showOrg() && !organization }
                                    />
                                );
                            })
                            }
                        </Table>
                        }

                        { pipelines && <ShowMoreButton pager={this.pager} /> }
                    </article>
                </main>
            </Page>
        );
    }
}

const { func, object } = PropTypes;

Pipelines.contextTypes = {
    params: object,
    store: object,
    router: object,
    pipelineService: object,
    location: object.isRequired, // From react-router
};

Pipelines.propTypes = {
    setTitle: func,
    params: object,
};

export default documentTitle(Pipelines);
