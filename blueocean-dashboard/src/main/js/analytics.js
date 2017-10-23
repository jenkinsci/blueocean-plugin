/* eslint-disable object-shorthand */
import { analyticsService } from '@jenkins-cd/blueocean-core-js';

/** codifies all events send to analytics service in this module */
class Analytics {
    trackPageView() {
        analyticsService.track('pageview', { mode: 'blueocean' });
    }

    trackPipelineActivity() {
        analyticsService.track('pipeline_activity_visited');
    }

    trackPipelineBranches() {
        analyticsService.track('pipeline_branches_visited');
    }

    trackPipelinePullRequests() {
        analyticsService.track('pipeline_pull_requests_visited');
    }

    trackDashboardVisited() {
        analyticsService.track('dashboard_visited');
    }

    trackPipelineCreationStarted() {
        analyticsService.track('pipeline_creation_started');
    }

    trackPipelineCreationProviderClicked(type) {
        const props = { type: type };
        analyticsService.track('pipeline_creation_provider_clicked', props);
    }

    trackPipelineRunVisited() {
        analyticsService.track('pipeline_run_visited');
    }
}

const analytics = new Analytics();

export {
    analytics,
};

/* eslint-enable object-shorthand */
