import { Route, Redirect, IndexRoute, IndexRedirect } from 'react-router';
import React from 'react';
import Dashboard from './Dashboard';
import OrganisationPipelines from './OrganisationPipelines';
import {
    Pipelines,
    MultiBranch,
    Activity,
    PullRequests,
    PipelinePage,
    RunDetails,
    RunDetailsPipeline,
    RunDetailsChanges,
    RunDetailsArtifacts,
    RunDetailsTests,
} from './components';

export default (
    <Route path="/" component={Dashboard}>
        <Route path="organizations/:org" component={OrganisationPipelines}>
            <IndexRedirect to="pipelines" />
            <Route path="pipelines" component={Pipelines} />

            <Route component={PipelinePage}>
                <Route path=":pipeline/branches" component={MultiBranch} />
                <Route path=":pipeline/activity" component={Activity} />
                <Route path=":pipeline/pr" component={PullRequests} />

                <Route path=":pipeline/detail/:branch/:runId" component={RunDetails}>
                    <IndexRedirect to="pipeline" />
                    <Route path="pipeline" component={RunDetailsPipeline} />
                    <Route path="changes" component={RunDetailsChanges} />
                    <Route path="tests" component={RunDetailsTests} />
                    <Route path="artifacts" component={RunDetailsArtifacts} />
                </Route>

                <Redirect from=":pipeline/*" to=":pipeline/activity" />
            </Route>
        </Route>
        <Route path="/pipelines" component={OrganisationPipelines}>
            <IndexRoute component={Pipelines} />
        </Route>
        <IndexRedirect to="pipelines" />
    </Route>
);
