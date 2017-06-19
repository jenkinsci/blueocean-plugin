import React, { PropTypes } from 'react';

import { Icon } from '@jenkins-cd/react-material-icons';
import { buildPipelineUrl } from '../util/UrlUtils';
import { Link } from 'react-router';

const RunHistoryButton = (props) => {
    const { pipeline, branchName } = props;
    const historyButtonUrl = `${buildPipelineUrl(pipeline.organization, pipeline.fullName)}/activity?branch=${encodeURIComponent(branchName)}`;

    return (<div className="history-button-component">
        <Link to={historyButtonUrl} className="materials-icons history-button">
            <Icon size={24} icon="history" />
        </Link>
    </div>);
};

RunHistoryButton.propTypes = {
    pipeline: PropTypes.object.isRequired,
    branchName: PropTypes.string.isRequired,
};

export default RunHistoryButton;
