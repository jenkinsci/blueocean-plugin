import React, { PropTypes, Component } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { BranchService } from '../index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.BranchPager');

@observer
export class Branch extends Component {
    componentWillMount() {
        const { url } = this.props;
        try {
            this.pager = BranchService.branchPager(url);
        } catch (e) {
            logger.error(e);
        }
    }
    render() {
        const { displayName, t } = this.props;
        const branchLabel = t('rundetail.header.branch', { defaultValue: 'Branch' });
        const pullRequestLabel = t('rundetail.header.pullRequest', { defaultValue: 'PR' });
        let branchSlug;
        let label = branchLabel;
        if (this.pager.pending) {
            branchSlug = (<span>{ displayName }</span>);
        } else {
            const style = { fill: '#ffffff' };
            branchSlug = (<span className="killSpace">
                { displayName }
                <a href={this.pager.branch.branch.url} target="_blank">
                    <Icon { ...{ style, size: 16, icon: 'launch' } } />
                </a>
            </span>);
            if (this.pager.branch.pullRequest) {
                label = pullRequestLabel;
            }
        }
        return (<div className="u-label-value" title={`${label}: ${displayName}`} >
            <label>{ label }:</label>
            { branchSlug}
        </div>);
    }
}

Branch.propTypes = {
    url: PropTypes.string.isRequired,
    t: PropTypes.func,
    displayName: PropTypes.string,
};
