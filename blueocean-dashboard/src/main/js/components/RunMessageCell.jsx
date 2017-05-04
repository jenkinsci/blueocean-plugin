import React, { Component, PropTypes } from 'react';
import Lozenge from './Lozenge';

export default class RunMessageCell extends Component {
    propTypes = {
        run: PropTypes.object,
        t: PropTypes.func,
    };

    render() {
        const run = this.props.run;
        const t = this.props.t;
        let message;
        if (run && run.description) {
            message = (<span className="RunMessageCell" title={run.description}><span className="RunMessageCellInner">{run.description}</span></span>);
        } else if (run && run.changeSet && run.changeSet.length > 0) {
            const commitMsg = run.changeSet[run.changeSet.length - 1].msg;
            if (run.changeSet.length > 1) {
                message = (<span className="RunMessageCell" title={commitMsg}><span className="RunMessageCellInner">{commitMsg}</span> <Lozenge title={ t('lozenge.commit', { 0: run.changeSet.length }) } /></span>);
            } else {
                message = (<span className="RunMessageCell" title={commitMsg}><span className="RunMessageCellInner">{commitMsg}</span></span>);
            }
        } else if (run && run.causes.length > 0) {
            const cause = run.causes[0].shortDescription;
            message = (<span className="RunMessageCell" title={cause}><span className="RunMessageCellInner">{cause}</span></span>);
        } else {
            message = (<span className="RunMessageCell"><span className="RunMessageCellInner">–</span></span>);
        }
        return message;
    }
}
