import React, { PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { BranchService } from '../index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.BranchPager');

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
        if (this.pager.pending) {
            logger.debug('Returning null');
            return null;
        }
        return (<div>branch {this.pager.branch}</div>);
    }
}

Branch.propTypes = {
    url: PropTypes.string.isRequired,
};
