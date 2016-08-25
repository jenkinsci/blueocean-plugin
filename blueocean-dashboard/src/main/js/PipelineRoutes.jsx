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

function goTo(path) {
    let next = path;
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
const runDetailRoutes = { path: ':runId', component: RunDetails,
    indexRoute: goTo('pipeline'),
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
    { path: '', component: Dashboard, indexRoute: goTo('pipelines'),
        childRoutes: [
        { path: 'organizations/:organization', component: OrganizationPipelines,
            indexRoute: goTo('pipelines'),
            childRoutes: [

            { path: 'pipelines', component: Pipelines },

            { path: ':pipeline', component: PipelinePage,
                indexRoute: goTo('activity'),
                childRoutes: [
                    { path: 'branches', component: MultiBranch },

                    { path: 'activity', component: Activity },

                    { path: 'pr', component: PullRequests },

                    { path: 'detail', childRoutes: [ runDetailRoutes ] },
                    
                    { path: 'detail/:branch', childRoutes: [ runDetailRoutes ] },

                    DynamicRoutes,
                ] },
            ] },
        { path: '/pipelines', component: OrganizationPipelines, indexRoute: {
            component: Pipelines,
        } },
        ] }
);
