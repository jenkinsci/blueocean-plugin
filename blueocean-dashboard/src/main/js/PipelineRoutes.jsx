import { Route, Redirect, IndexRoute, IndexRedirect } from 'react-router';
import React from 'react';
import Dashboard from './Dashboard';
import OrganizationPipelines from './OrganizationPipelines';
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
import Extensions, { dataType } from '@jenkins-cd/js-extensions';

// FIXME, this needs to fetch extensions iteself, like the app does

let pipelinePluginRoutes = {};
const DynamicRoutes = {
    path: 'course/:courseId',

    getChildRoutes(partialNextState, callback) {
        require.ensure([], function (require) {
            callback(null, [
                require('./routes/Announcements'),
            ])
        })
    },

    getIndexRoute(partialNextState, callback) {
        require.ensure([], function (require) {
            callback(null, {
                component: require('./components/Index'),
            })
        })
    },

    getComponents(nextState, callback) {
        require.ensure([], function (require) {
            callback(null, require('./components/Course'))
        })
    }
};

Extensions.store.getExtensions(['pipeline.routes'], (routes = []) => {
    pipelinePluginRoutes = { ...pipelinePluginRoutes, routes };
});

export default (
    <Route path="/" component={Dashboard}>
        <Route path="organizations/:organization" component={OrganizationPipelines}>
            <IndexRedirect to="pipelines" />
            <Route path="pipelines" component={Pipelines} />

            <Route component={PipelinePage}>
                <Route path=":pipeline/branches" component={MultiBranch} />
                <Route path=":pipeline/activity" component={Activity} />
                <Route path=":pipeline/pr" component={PullRequests} />

                <DynamicRoutes />
                
                <Route path=":pipeline/detail/:branch/:runId" component={RunDetails}>
                    <IndexRedirect to="pipeline" />
                    <Route path="pipeline" component={RunDetailsPipeline} >
                        <Route path=":node" component={RunDetailsPipeline} />
                    </Route>
                    <Route path="changes" component={RunDetailsChanges} />
                    <Route path="tests" component={RunDetailsTests} />
                    <Route path="artifacts" component={RunDetailsArtifacts} />
                </Route>

                <Redirect from=":pipeline(/*)" to=":pipeline/activity" />
            </Route>
        </Route>
        <Route path="/pipelines" component={OrganizationPipelines}>
            <IndexRoute component={Pipelines} />
        </Route>
        <IndexRedirect to="pipelines" />
    </Route>
);
