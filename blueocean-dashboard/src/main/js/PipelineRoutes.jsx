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

const DynamicRoutes = {
    path: ':pipeline/(.*)',

    getChildRoutes(partialNextState, callback) {
        Extensions.store.getExtensions('pipeline.routes', routes => {
            callback(null, routes); // routes is array
        });
    },
};

export default (
    {path: "", component: Dashboard, indexRoute:{
        onEnter: ({ params }, replace) => replace('pipelines'),
    },
        childRoutes: [
        {path: "organizations/:organization", component: OrganizationPipelines,
            indexRoute:{ onEnter: ({ params }, replace) => replace(`/organizations/${params.organization}/pipelines`) },
            childRoutes: [
            {path: "pipelines", component: Pipelines},

            {component: PipelinePage, childRoutes: [
                {path: ":pipeline/branches", component: MultiBranch},
                {path: ":pipeline/activity", component: Activity},
                {path: ":pipeline/pr", component: PullRequests},

                {path: ":pipeline/detail/:branch/:runId", component: RunDetails,
                    indexRoute: {
                        onEnter: ({ params }, replace) => replace(
                            `/organizations/${params.organization}/${encodeURIComponent(params.pipeline)}/` +
                            `detail/${params.branch}/${params.runId}/pipeline`
                        ),
                    },
                    childRoutes: [
                    {path: "pipeline", component: RunDetailsPipeline, childRoutes: [
                        {path: ":node", component: RunDetailsPipeline},
                    ]},
                    {path: "changes", component: RunDetailsChanges},
                    {path: "tests", component: RunDetailsTests},
                    {path: "artifacts", component: RunDetailsArtifacts},
                ]},

                DynamicRoutes,
                // TODO: need to convert this to onEnter - somehow?
                //<Redirect from=":pipeline(/*)" to=":pipeline/activity" />
            ]}
        ]},
        {path: "/pipelines", component: OrganizationPipelines, indexRoute: {
            component: Pipelines,
        }}
    ]}
);
