import { analyticsService } from '@jenkins-cd/blueocean-core-js';

/** codifies all events send to analytics service in this module */
class Analytics {
    trackPageView() {
        analyticsService.track('pageview', { mode: 'blueocean' });
    }

    trackPipelineActivityVisited() {
        analyticsService.track('pipeline_activity_visited');
    }

    trackPipelineBranchesVisited() {
        analyticsService.track('pipeline_branches_visited');
    }

    trackPipelinePullRequestsVisited() {
        analyticsService.track('pipeline_pull_requests_visited');
    }

    trackDashboardVisited() {
        analyticsService.track('dashboard_visited');
    }

    trackPipelineCreationVisited() {
        analyticsService.track('pipeline_creation_visited');
    }

    trackPipelineRunVisited() {
        analyticsService.track('pipeline_run_visited');
    }

    trackPipelineRunChangesVisited() {
        analyticsService.track('pipeline_run_changes_visited');
    }

    trackPipelineRunTestsVisited() {
        analyticsService.track('pipeline_run_tests_visited');
    }

    trackPipelineRunArtifactsVisited() {
        analyticsService.track('pipeline_run_artifacts_visited');
    }
}

const analytics = new Analytics();

export { analytics };
