import React, {PropTypes} from 'react';
import {observer} from 'mobx-react';
import { FilterableList } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import {i18nTranslator} from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');


@observer
export default class PerforceProjectListStep extends React.Component {
    selectProject(proj) {
        console.log("PerforceProjectListStep.selectProject().proj: " + proj);
        this.props.flowManager.selectProject(proj);
    }

    beginCreation() {
        this.props.flowManager.saveRepo();
    }

    render() {
        const {flowManager, title = 'creation.p4.project_step.title'} = this.props;
        const titleString = t(title);
        const disabled = flowManager.stepsDisabled;
        const buttonDisabled = !flowManager.selectedProject;

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={titleString} disabled={disabled}>
                <div className="container">
                <FilterableList
                    className="repo-list"
                    data={flowManager.projects}
                    onItemSelect={(idx, proj) => this.selectProject(proj)}
                    labelFunction={proj => proj}
                    filterFunction={(text, proj) => proj.toLowerCase().indexOf(text.toLowerCase()) !== -1}
                />
                <button className="button-create" onClick={(proj) => this.beginCreation(proj)}
                        disabled={buttonDisabled}>
                    {t('creation.core.header.title')}
                </button>
                </div>
            </FlowStep>
        );
    }
}

PerforceProjectListStep.propTypes = {
    flowManager: PropTypes.object,
    title: PropTypes.string,
};
