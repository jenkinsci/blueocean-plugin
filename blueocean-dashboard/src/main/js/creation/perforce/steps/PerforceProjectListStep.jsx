import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

function ProjectRenderer(props) {
    const { listItem } = props;
    const { name, avatar } = listItem;

    return (
        <div className="org-list-item">
            <img className="avatar" width="30" height="30" src={`${avatar}`} />
            <span>{name}</span>
        </div>
    );
}

ProjectRenderer.propTypes = {
    listItem: PropTypes.object,
};

@observer
export default class PerforceProjectListStep extends React.Component {
    selectProject(proj) {
        this.props.flowManager.selectProject(proj);
        console.log("PerforceProjectListStep: " + proj.loginName);
    }

    beginCreation() {
        this.props.flowManager.saveRepo();
    }

    render() {
        const { flowManager, title = 'creation.p4.project_step.title' } = this.props;

        const titleString = t(title);
        const disabled = flowManager.stepsDisabled;
        const buttonDisabled = !flowManager.selectedProject;

        return (
            <FlowStep {...this.props} title={titleString} disabled={disabled}>
                <List
                    data={flowManager.projects}
                    onItemSelect={(idx, proj) => this.selectProject(proj)}
                    labelFunction={proj => proj.loginName}
                    defaultContainerClass={false}
                />
                <button className="button-create" onClick={(proj) => this.beginCreation(proj)} disabled={buttonDisabled}>
                    {t('creation.core.header.title')}
                </button>
            </FlowStep>
        );
    }
}

PerforceProjectListStep.propTypes = {
    flowManager: PropTypes.object,
    title: PropTypes.string,
};
