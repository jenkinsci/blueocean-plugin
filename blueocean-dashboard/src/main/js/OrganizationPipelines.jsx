import React, { Component, PropTypes } from 'react';
import OrganisationPipelines from './OrganisationPipelines';

class OrganizationPipelines extends Component {

    render() {
        const organization = this.context.params.organization;

        return (
            <OrganisationPipelines { ... this.props } organization={organization} />
        );
    }

}

OrganizationPipelines.contextTypes = {
    params: PropTypes.object,
};

export default OrganizationPipelines;
