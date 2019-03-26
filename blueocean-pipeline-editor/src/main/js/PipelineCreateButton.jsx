import React from 'react';
import PropTypes from 'prop-types';

export default class PipelineCreateButton extends React.Component {
    render() {
        const { organization, fullName } = this.props;
        const parts = fullName.split('/');
        const url = `/organizations/${organization}/pipeline-editor/${encodeURIComponent(fullName)}/`;
        return <button onClick={() => this.context.router.push(url)}>Create Pipeline</button>;
    }
}

PipelineCreateButton.propTypes = {
    organization: PropTypes.string,
    fullName: PropTypes.string,
};

PipelineCreateButton.contextTypes = {
    router: PropTypes.object,
};
