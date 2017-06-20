import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, JTable, TableHeaderRow } from '@jenkins-cd/design-language';
import { i18nTranslator, ContentPageHeader, AppConfig, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { Icon } from '@jenkins-cd/react-material-icons';

import { documentTitle } from './DocumentTitle';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import { DashboardPlaceholder } from './placeholder/DashboardPlaceholder';
import updateGetParam from '../util/UpdateGetParam';

const translate = i18nTranslator('blueocean-dashboard');

@observer
export class Pipelines extends Component {

    state = {
        actionExtensionCount: 0,
    };

    componentWillMount() {
        this.setState({ searchText: this.getSearchText() });
        this._countExtensions();
    }

    onChange = value => {
        this.setState({ searchText: value });
        this.updateSearchText(value);
    };

    getSearchText() {
        return this.props.location.query.search ? decodeURIComponent(this.props.location.query.search) : '';
    }

    updateSearchText = debounce(value => {
        this.context.router.push(`${this.props.location.pathname}${updateGetParam('search', encodeURIComponent(value), this.props.location.query)}`);
    }, 200);

    clearSearchInputText = () => {
        this.setState({ searchText: '' });
        this.context.router.push(`${this.props.location.pathname}${updateGetParam('search', '', this.props.location.query)}`);
    };

    // Figure out how many extensions we have for the action buttons column so we can size it appropriately
    _countExtensions() {
        Extensions.store.getExtensions('jenkins.pipeline.list.action', extensions => {
            const count = extensions && typeof(extensions.length) === 'number' ? extensions.length : 0;
            if (count !== this.state.actionExtensionCount) {
                this.setState({ actionExtensionCount: count });
            }
        });
    }

    render() {
        const { organization } = this.context.params;
        const { actionExtensionCount } = this.state;
        const organizationName = organization || AppConfig.getOrganizationName();
        const organizationDisplayName = organization === AppConfig.getOrganizationName() ? AppConfig.getOrganizationDisplayName() : organization;

        const searchText = this.getSearchText();
        this.pager = this.context.pipelineService.pipelinesPager(organizationName, searchText);
        const pipelines = this.pager.data;

        const orgLink = organizationName ? (
            <Link to={ `organizations/${organizationName}` }>
                { organizationDisplayName }
            </Link>
        ) : '';

        const showPipelineList = pipelines && pipelines.length > 0;
        const showEmptyState = !this.pager.pending && !this.getSearchText() && (!pipelines || !pipelines.length);

        const labelName = translate('home.pipelineslist.header.name', { defaultValue: 'Name' });
        const labelHealth = translate('home.pipelineslist.header.health', { defaultValue: 'Health' });
        const labelBranches = translate('home.pipelineslist.header.branches', { defaultValue: 'Branches' });
        const labelPullReqs = translate('home.pipelineslist.header.pullrequests', { defaultValue: 'PR' });

        const columns = [
            JTable.column(640, labelName, true),
            JTable.column(70, labelHealth),
            JTable.column(70, labelBranches),
            JTable.column(70, labelPullReqs),
            JTable.column(actionExtensionCount * 24, ''),
        ];

        const pipelineRows = pipelines && pipelines.map(pipeline => {
            const key = pipeline._links.self.href;
            return (
                <PipelineRowItem
                    t={ translate }
                    key={ key } pipeline={ pipeline }
                    showOrganization={ AppConfig.showOrg() && !organizationName }
                />
            );
        });

        this.props.setTitle('Jenkins Blue Ocean');

        return (
            <Page>
                <ContentPageHeader>
                    <div className="u-flex-grow">
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.header">
                            <h1>
                                <Link to="/">
                                    { translate('home.header.dashboard', { defaultValue: 'Dashboard' }) }
                                </Link>
                                { AppConfig.showOrg() && organizationName && ' / ' }
                                { AppConfig.showOrg() && organizationName && orgLink }
                            </h1>
                        </Extensions.Renderer>

                        <div className="TextInput search-pipelines-input u-icon-left" iconLeft="search">
                            <div className="TextInput-icon u-icon-left">
                                <Icon icon="search" />
                            </div>
                            <input className="fastsearch-input TextInput-control" value={this.state.searchText} placeholder="Search pipelines..." onChange={(e) => {this.onChange(e.target.value ? e.target.value : '');}} />
                            <div className="TextInput-icon clear-icon-container" onClick={this.clearSearchInputText}>
                                <Icon icon="clear" />
                            </div>
                        </div>
                    </div>
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                        <CreatePipelineLink />
                    </Extensions.Renderer>
                </ContentPageHeader>
                <main>
                    <article>
                        {!this.getSearchText() &&
                            <Extensions.Renderer
                                extensionPoint="jenkins.pipeline.list.top"
                                store={ this.context.store }
                                router={ this.context.router }
                            />
                        }
                        { showEmptyState && <DashboardPlaceholder t={translate} /> }
                        { !this.pager.pending && !pipelines.length && this.getSearchText() &&
                            <div className="no-search-results-container">
                                There are no pipelines that match <i>{this.getSearchText()}</i>
                            </div>
                        }
                        { showPipelineList && (
                            <JTable className="pipelines-table" columns={ columns } >
                                <TableHeaderRow />
                                { pipelineRows }
                            </JTable>
                        )}
                        { (pipelines || this.pager.pending) && <ShowMoreButton pager={this.pager} /> }
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
};

Pipelines.propTypes = {
    setTitle: func,
    params: object,
    router: object,
    location: object,
};

export default documentTitle(Pipelines);
