import { Route, IndexRoute } from 'react-router';
import React from 'react';
import OrganisationPipelines from './OrganisationPipelines';
import {
    Pipelines,
    MultiBranch,
    Activity,
    PullRequests,
    PipelinePage,
    RunDetails,
} from './components';

// Config has some globals in it for path / routes
import { rootRoutePath } from './config';

export default (
    <Route path={rootRoutePath} component={OrganisationPipelines}>
        <IndexRoute component={Pipelines} />
        <Route component={PipelinePage}>
            <Route path=":pipeline/branches" component={MultiBranch} />
            <Route path=":pipeline/activity" component={Activity} />
            <Route path=":pipeline/pr" component={PullRequests} />
        </Route>
        <Route
          path=":pipeline/detail/:branch/:runId"
          component={RunDetails}
        />
    </Route>
);
