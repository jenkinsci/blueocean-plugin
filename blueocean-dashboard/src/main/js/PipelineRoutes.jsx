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

const DynamicRoutes = {
    path: ':pipeline/(.*)',

    getChildRoutes(partialNextState, callback) {
        Extensions.store.getExtensions('pipeline.routes', routes => {
            callback(null, routes); // routes is array
        });
    },
};

export default (
    {path: "/", component: Dashboard, childRoutes: [
        {path: "organizations/:organization", component: OrganizationPipelines, childRoutes: [
            //<IndexRedirect to="pipelines" />
            {path: "pipelines", component: Pipelines},

            {component: PipelinePage, childRoutes: [
                {path: ":pipeline/branches", component: MultiBranch},
                {path: ":pipeline/activity", component: Activity},
                {path: ":pipeline/pr", component: PullRequests},

                {path: ":pipeline/detail/:branch/:runId", component: RunDetails, childRoutes: [
                    //<IndexRedirect to="pipeline" />
                    {path: "pipeline", component: RunDetailsPipeline, childRoutes: [
                        {path: ":node", component: RunDetailsPipeline},
                    ]},
                    {path: "changes", component: RunDetailsChanges},
                    {path: "tests", component: RunDetailsTests},
                    {path: "artifacts", component: RunDetailsArtifacts},
                ]},

                DynamicRoutes,
                
                //<Redirect from=":pipeline(/*)" to=":pipeline/activity" />
            ]}
        ]},
        
        {path: "/pipelines", component: OrganizationPipelines, childRoutes: [
            //<IndexRoute, component: Pipelines} />
        ]}
        //<IndexRedirect to="pipelines" />
    ]}
);
