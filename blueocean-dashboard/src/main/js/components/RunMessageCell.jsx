import React, { Component, PropTypes } from 'react';
import Lozenge from './Lozenge';
import { Link } from 'react-router';

export default class RunMessageCell extends Component {
    propTypes = {
        run: PropTypes.object,
        t: PropTypes.func,
        linkTo: PropTypes.string,
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
                        <Link to={linkTo} className="unstyled-link" >
                            {run.description}
                        </Link>
                    </span>
                </span>
            );
        } else if (showCommitMessage) {
            let commitMsg = run.changeSet[run.changeSet.length - 1].msg;
            const commitMsgWithIssues = [];
            const linkedCommitMsg = (<Link to={linkTo} className="unstyled-link" >{commitMsg}</Link>);

            if (run.changeSet[run.changeSet.length - 1].issues) {
                let issuesIdString = '';
                const issuesObj = {};

                for (const issue of run.changeSet[run.changeSet.length - 1].issues) {
                    issuesIdString += `${issue.id}|`;
                    issuesObj[issue.id] = issue.url;
                }

                if (issuesIdString) {
                    const issuesRegExpString = new RegExp(`(${issuesIdString.slice(0, -1)})`, 'gi');

                    for (let commitMsgPart of commitMsg.split(issuesRegExpString)) {
                        if (issuesObj[commitMsgPart]) {
                            commitMsgWithIssues.push(<a href={issuesObj[commitMsgPart]} target="_blank">{commitMsgPart}</a>);
                        } else {
                            if (commitMsgPart) {
                                commitMsgWithIssues.push((<Link to={linkTo} className="unstyled-link" >{commitMsgPart}</Link>));
                            }
                        }
                    }
                }
            }

            if (run.changeSet.length > 1) {
                return (
                    <span className="RunMessageCell" title={commitMsg}>
                        <span className="RunMessageCellInner">
                            {commitMsgWithIssues.length ? commitMsgWithIssues : linkedCommitMsg}
                        </span>
                        <Lozenge title={t('lozenge.commit', { 0: run.changeSet.length })} />
                    </span>
                );
            }
            
            return (
                <span className="RunMessageCell" title={commitMsg}>
                    <span className="RunMessageCellInner">
                        {commitMsgWithIssues.length ? commitMsgWithIssues : linkedCommitMsg}
                    </span>
                </span>
            );
        } else if (showCauses) {
            // Last cause is always more significant than the first
            const cause = run.causes[run.causes.length - 1].shortDescription;
            const linkedCauseMsg = (<Link to={linkTo} className="unstyled-link" >{ cause }</Link>);
            return (
                <span className="RunMessageCell" title={cause}>
                    <span className="RunMessageCellInner">
                        {linkedCauseMsg}
                    </span>
                </span>
            );
        } else {
            message = (<span className="RunMessageCell"><span className="RunMessageCellInner">–</span></span>);
        }
        return message;
    }
}
