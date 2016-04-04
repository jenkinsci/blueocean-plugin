import { Route, IndexRoute } from 'react-router';
import React from 'react';
import OrganisationPipelines from './OrganisationPipelines';
import { Pipelines, MultiBranch, Activity, PullRequests } from './components';

// Config has some globals in it for path / routes
import { rootRoutePath } from './config';

export default (
    <Route path={rootRoutePath} component={OrganisationPipelines}>
        <IndexRoute component={Pipelines} />
        <Route path=":pipeline/branches" component={MultiBranch} />
        <Route path=":pipeline/activity" component={Activity} />
        <Route path=":pipeline/pr" component={PullRequests} />
    </Route>
);
