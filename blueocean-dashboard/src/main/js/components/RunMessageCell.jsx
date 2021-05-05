import React, { Component, PropTypes } from 'react';
import Lozenge from './Lozenge';
import { Link } from 'react-router';
import LinkifiedText from './LinkifiedText';
import { UrlConfig } from '@jenkins-cd/blueocean-core-js';

export default class RunMessageCell extends Component {
    propTypes = {
        run: PropTypes.object,
        t: PropTypes.func,
        linkTo: PropTypes.string,
        changesUrl: PropTypes.string,
    };

    render() {
        const run = this.props.run;
        const t = this.props.t;
        const linkTo = this.props.linkTo || '';
        let message;

        // Note that the order that this is evaluated is important for providing a relevant message to the user

        // 1. If the user has set a message then we always show it
        const showUserDefinedMessage = run && run.description;

        // 2. Show commit messages if available
        //    however if a run has > 1 cause then the cause is likely more important than the change set (e.g. replay)
        const showCommitMessage = run && run.changeSet && (run.changeSet && run.changeSet.length > 0) && (run.causes && run.causes.length <= 1);

        // 3. Lastly if there are any causes, display the last cause available
        const showCauses = run && (run.causes && run.causes.length > 0);

        if (showUserDefinedMessage) {
            message = (
                <span className="RunMessageCell" title={run.description}>
                    <span className="RunMessageCellInner">
                        <Link to={linkTo} className="unstyled-link">
                            {run.description}
                        </Link>
                    </span>
                </span>
            );
        } else if (showCommitMessage) {
            const commitMsg = run.changeSet[run.changeSet.length - 1].msg;

            if (run.changeSet.length > 1) {
                const { changesUrl } = this.props;

                return (
                    <span className="RunMessageCell" title={commitMsg}>
                        <span className="RunMessageCellInner">
                            <LinkifiedText text={commitMsg} textLink={linkTo} partialTextLinks={run.changeSet[run.changeSet.length - 1].issues} />
                        </span>
                        <Lozenge title={t('lozenge.commit', { 0: run.changeSet.length })} linkTo={changesUrl} />
                    </span>
                );
            }

            return (
                <span className="RunMessageCell" title={commitMsg}>
                    <span className="RunMessageCellInner">
                        <LinkifiedText text={commitMsg} textLink={linkTo} partialTextLinks={run.changeSet[run.changeSet.length - 1].issues} />
                    </span>
                </span>
            );
        } else if (showCauses) {
            const lastCause = (run && run.causes && run.causes.length > 0 && run.causes[run.causes.length - 1]) || null;
            const cause = (lastCause && lastCause.shortDescription) || null;

            if (lastCause && lastCause.upstreamProject) {
                const activityUrl = `${UrlConfig.getJenkinsRootURL()}/${lastCause.upstreamUrl}display/redirect?provider=blueocean`;
                const runUrl = `${UrlConfig.getJenkinsRootURL()}/${lastCause.upstreamUrl}${lastCause.upstreamBuild}/display/redirect?provider=blueocean`;

                return (
                    <span className="RunMessageCell" title={cause}>
                        Started by upstream pipeline "<a href={activityUrl}>{lastCause.upstreamProject}</a>" build&nbsp;{' '}
                        <a href={runUrl}>#{lastCause.upstreamBuild}</a>
                    </span>
                );
            }

            const linkedCauseMsg = (
                <Link to={linkTo} className="unstyled-link">
                    <span className="ellipsis-text">{cause}</span>
                </Link>
            );

            return (
                <span className="RunMessageCell" title={cause}>
                    <span className="RunMessageCellInner">{linkedCauseMsg}</span>
                </span>
            );
        } else {
            message = (
                <span className="RunMessageCell">
                    <span className="RunMessageCellInner">–</span>
                </span>
            );
        }
        return message;
    }
}
