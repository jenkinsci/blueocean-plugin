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
import { relativeUrl } from './util/UrlUtils';
import Extensions from '@jenkins-cd/js-extensions';

const DynamicRoutes = {
    path: ':pipeline/(.*)',

    getChildRoutes(partialNextState, callback) {
        Extensions.store.getExtensions('pipeline.routes', routes => {
            callback(null, routes); // routes is array
        });
    },
};

function GoTo(path) {
    var next = path;
    while (next.charAt(0) === '/') {
        next = next.substring(1);
    }
    return {
        onEnter: ({ location, params }, replace) => {
            replace(relativeUrl(location, next));
        },
    };
}

// multibranch-pr-github/detail/test-pass-fail-stage/1/pipeline
// :pipeline/detail/:branch/:runId/pipeline
const RunDetailRoutes = { path: 'detail(/branch/:branch)/:runId', component: RunDetails,
    indexRoute: GoTo('pipeline'),
    childRoutes: [
        { path: 'pipeline', component: RunDetailsPipeline, childRoutes: [
            { path: ':node', component: RunDetailsPipeline },
        ] },
        { path: 'changes', component: RunDetailsChanges },
        { path: 'tests', component: RunDetailsTests },
        { path: 'artifacts', component: RunDetailsArtifacts },
    ],
};

export default (
    { path: '', component: Dashboard, indexRoute: GoTo('pipelines'),
        childRoutes: [
        { path: 'organizations/:organization', component: OrganizationPipelines,
            indexRoute: GoTo('pipelines'),
            childRoutes: [

            { path: 'pipelines', component: Pipelines },

            { path: ':pipeline', component: PipelinePage,
                indexRoute: GoTo('activity'),
                childRoutes: [

                { path: 'branches', component: MultiBranch,
                    childRoutes: [{ path: 'detail(/branch/:branch)/:runId', component: RunDetails,
    indexRoute: GoTo('pipeline'),
    childRoutes: [
        { path: 'pipeline', component: RunDetailsPipeline, childRoutes: [
            { path: ':node', component: RunDetailsPipeline },
        ] },
        { path: 'changes', component: RunDetailsChanges },
        { path: 'tests', component: RunDetailsTests },
        { path: 'artifacts', component: RunDetailsArtifacts },
    ],
}] },

                { path: 'activity', component: Activity,
                    childRoutes: [RunDetailRoutes] },

                { path: 'pr', component: PullRequests,
                    childRoutes: [RunDetailRoutes] },

                    DynamicRoutes,
                ] },
            ] },
        { path: '/pipelines', component: OrganizationPipelines, indexRoute: {
            component: Pipelines,
        } },
        ] }
);
