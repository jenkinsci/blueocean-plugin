/**
 * Created by cmeyers on 10/20/16.
 */
import React from 'react';
import { Link } from 'react-router';
import { Security } from '@jenkins-cd/blueocean-core-js';

export default function CreatePipelineLink() {
    if (Security.isSecurityEnabled() && Security.isAnonymousUser()) {
        return null;
    }

    return (
        <Link to="/create-pipeline" className="btn-secondary inverse">
            New Pipeline
        </Link>
    );
}
