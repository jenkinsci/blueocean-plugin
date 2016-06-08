import React, { Component, PropTypes } from 'react';
import OrganisationPipelines from './OrganisationPipelines';

class AllPipelines extends Component {

    render() {
        return (
            <OrganisationPipelines { ... this.props } />
        );
    }

}

export default AllPipelines;
