import React from 'react';

export default class FreestyleDeployments extends React.Component {
    render() {
        return <div>Some deployments</div>;
    }
}

FreestyleDeployments.contextTypes = {
    pipeline: React.PropTypes.any,
};
