import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { FilterableList } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

@observer
export default class BbRepositoryStep extends React.Component {

    selectRepository(org) {
        this.props.flowManager.selectRepository(org);
    }

    beginCreation() {
        this.props.flowManager.saveRepo();
    }

    _getLoadingMessage() {
        const { repositoriesLoading } = this.props.flowManager;
        const count = this.props.flowManager.repositories.length;

        if (repositoriesLoading) {
            return t('creation.core.repository.loading.count', { 0: count });
        }

        return t('creation.core.repository.loaded.count', { 0: count });
    }

    _exit() {
        this.props.flowManager.completeFlow();
    }

    _sortRepos(a, b) {
        return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
    }

    render() {
        const { flowManager } = this.props;
        const title = t('creation.core.repository.choose.title');
        const disabled = flowManager.stepsDisabled;
        const buttonDisabled = !flowManager.selectedRepository;
        const orgName = flowManager.selectedOrganization.name;
        const sortedRepos = flowManager.selectableRepositories.slice().sort(this._sortRepos);
        const loading = flowManager.repositoriesLoading;

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title} loading={loading} disabled={disabled}>
                <div className="loading-msg">
                    { this._getLoadingMessage()}
                </div>

                { flowManager.selectableRepositories.length > 0 &&
                <div className="container">
                    <FilterableList
                        className="repo-list"
                        data={sortedRepos}
                        onItemSelect={(idx, repo) => this.selectRepository(repo)}
                        labelFunction={repo => repo.name}
                        filterFunction={(text, repo) => repo.name.toLowerCase().indexOf(text.toLowerCase()) !== -1}
                    />

                    <button
                        className="button-create"
                        onClick={() => this.beginCreation()}
                        disabled={buttonDisabled}
                    >
                        {t('creation.core.header.title')}
                    </button>
                </div>
                }

                { flowManager.repositories.length === 0 &&
                <div className="container">
                    <p className="instructions">
                        {t('creation.core.repository.no_repository', { 0: orgName })}

                        {t('creation.core.organization.pick_different')}
                    </p>

                    <button onClick={() => this._exit()}>{t('creation.core.intro.invalid_permission_button')}</button>
                </div>
                }
            </FlowStep>
        );
    }
}

BbRepositoryStep.propTypes = {
    flowManager: PropTypes.object,
};
