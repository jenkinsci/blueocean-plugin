import { Route } from 'react-router';
import React from 'react';
import FreestyleDeployments from './components/FreestyleDeployments';

export default (
    <Route path=":pipeline/deployments" component={FreestyleDeployments} />
);
