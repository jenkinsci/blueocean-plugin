import React from 'react';

export default class PipelineCreateButton extends React.Component {
    render() {
        const {organization, fullName} = this.props;
        const parts = fullName.split('/');
        const url = `/organizations/${organization}/pipeline-editor/${encodeURIComponent(fullName)}/`;
        return <button onClick={() => this.context.router.push(url)}>Create Pipeline</button>;
    }
}

PipelineCreateButton.propTypes = {
    prganization: React.PropTypes.string,
    fullName: React.PropTypes.string,
};

PipelineCreateButton.contextTypes = {
    router: React.PropTypes.object,
};
