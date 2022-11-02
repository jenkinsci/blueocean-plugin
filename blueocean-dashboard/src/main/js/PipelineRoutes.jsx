import { Route, Redirect, IndexRedirect } from 'react-router';
import React from 'react';
import { AppConfig, NotFound } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { analytics } from './analytics';

import Dashboard from './Dashboard';
import {
    Pipelines,
    MultiBranch,
    Activity,
    PullRequests,
    PipelinePage,
    PipelineTrends,
    RunDetails,
    RunDetailsPipeline,
} from './components';
import { CreatePipeline } from './creation';

/**
 * gets the background element used for the modal underlay
 */
function getBackgroundElement() {
    return document.getElementById('modal-snap-background');
}

/**
 * Cleans up the copied HTML to remove IDs, react root attributes
 * and other non-visible elements.
 */
function cleanupCopy(el) {
    el.removeAttribute('data-reactroot');
    el.removeAttribute('id');
    if (el.childNodes && el.childNodes.length) {
        for (let i = 0; i < el.childNodes.length; i++) {
            const child = el.childNodes[i];
            if (child.nodeType !== Node.TEXT_NODE && child.nodeType !== Node.ELEMENT_NODE) {
                el.removeChild(child);
            } else if (child.nodeType === Node.ELEMENT_NODE) {
                cleanupCopy(child);
            }
        }
    }
}

/**
 * Removes a persisted background, restores prior
 * scroll position
 */
function discardPersistedBackground() {
    const bg = getBackgroundElement();
    if (bg) {
        const scrollY = bg.getAttribute('scrollY');
        const scrollX = bg.getAttribute('scrollX');
        window.scroll(scrollX, scrollY);
        bg.parentElement.removeChild(bg);
    }
}

/**
 * Takes a snapshot of the react root, and overlays it
 */
function persistModalBackground() {
    const root = document.getElementById('root');
    const background = root.cloneNode(true);
    cleanupCopy(background);
    discardPersistedBackground();
    const container = document.createElement('div');
    container.id = 'modal-snap-background';
    container.appendChild(background);
    container.style.display = 'block';
    container.style.top = `${-1 * window.scrollY}px`;
    container.style.left = `${-1 * window.scrollX}px`;
    container.setAttribute('scrollY', window.scrollY);
    container.setAttribute('scrollX', window.scrollX);
    root.appendChild(container);
}

function isEnteringRunDetails(prevState, nextState) {
    return nextState.params.runId && (prevState == null || !prevState.params.runId);
}

function isLeavingRunDetails(prevState, nextState) {
    return prevState !== null && prevState.params.runId && !nextState.params.runId;
}

function isPersistBackgroundRoute(prevState, nextState) {
    return isEnteringRunDetails(prevState, nextState);
}

function isRemovePersistedBackgroundRoute(prevState, nextState) {
    return isLeavingRunDetails(prevState, nextState);
}

function onTopLevelRouteEnter() {
    analytics.trackPageView(); // Tracks the page view on load of window
}

/**
 * Persists the application's DOM as a "background" when navigating to a route w/ a modal or dialog.
 * Also removes the "background" when navigating away.
 * Note this must be done early (from top-level onChange handler) and can't wait until a modal/dialog will mount
 * due to the fact react router will have already changed the background context.
 */
function persistBackgroundOnNavigationChange(prevState, nextState, replace, callback, delay = 200) {
    if (isPersistBackgroundRoute(prevState, nextState)) {
        persistModalBackground();
    } else if (isRemovePersistedBackgroundRoute(prevState, nextState)) {
        // need to delay this a little to let the route re-render
        setTimeout(discardPersistedBackground, delay);
    }
    if (callback) {
        callback();
    }
}

function onRouteChange(prevState, nextState, replace, callback, delay = 200) {
    analytics.trackPageView(); // Tracks page view as the route changes
    persistBackgroundOnNavigationChange(prevState, nextState, replace, callback, delay);
}

function onLeaveCheckBackground() {
    persistBackgroundOnNavigationChange({ params: { runId: true } }, { params: {} }, null, null, 0);
}

const trends = AppConfig.isFeatureEnabled('trends');

function getActions(extensionPoint, params, callback) {
    Extensions.store.getExtensions([ extensionPoint ], Extensions.Utils.sortByOrdinal, (actions = []) => {
        const action = actions.map(a => new a()).find(action => action.name === params.action);
        if (action) {
            callback(null, action.component);
        } else {
            callback(null, NotFound);
        }
    });
}

function getRunDetailsAction({ params }, callback) {
    // TODO generify this
    switch (params.action) {
        case 'changes': analytics.trackPipelineRunChangesVisited(); break;
        case 'tests': analytics.trackPipelineRunTestsVisited(); break;
        case 'artifacts': analytics.trackPipelineRunArtifactsVisited(); break;
    }
    getActions('jenkins.run.actions', params, callback);
}

export default (
    <Route component={Dashboard} onEnter={onTopLevelRouteEnter} onChange={onRouteChange}>
        <Route path="organizations/:organization/pipelines" component={Pipelines} onEnter={analytics.trackDashboardVisited} />
        <Route path="organizations/:organization/create-pipeline" component={CreatePipeline} onEnter={analytics.trackPipelineCreationVisited} />
        <Redirect from="organizations/:organization(/*)" to="organizations/:organization/pipelines" />
        <Route path="organizations/:organization" component={PipelinePage}>
            <Route path=":pipeline/branches" component={MultiBranch} onEnter={analytics.trackPipelineBranchesVisited} />
            <Route path=":pipeline/activity" component={Activity} onEnter={analytics.trackPipelineActivityVisited} />
            <Route path=":pipeline/pr" component={PullRequests} onEnter={analytics.trackPipelinePullRequestsVisited} />
            {trends && <Route path=":pipeline/trends" component={PipelineTrends} />}

            <Route path=":pipeline/detail/:branch/:runId" component={RunDetails} onLeave={onLeaveCheckBackground}>
                <IndexRedirect to="pipeline" />
                <Route path="pipeline" component={RunDetailsPipeline} onEnter={analytics.trackPipelineRunVisited}>
                    <Route path=":node" component={RunDetailsPipeline} />
                </Route>
                <Route path=":action" getComponents={getRunDetailsAction} />
            </Route>

            <Redirect from=":pipeline(/*)" to=":pipeline/activity" />
        </Route>
        <Route path="/pipelines" component={Pipelines} onEnter={analytics.trackDashboardVisited} />
        <Route path="/create-pipeline" component={CreatePipeline} onEnter={analytics.trackPipelineCreationVisited} />
        <IndexRedirect to="pipelines" />
    </Route>
);
