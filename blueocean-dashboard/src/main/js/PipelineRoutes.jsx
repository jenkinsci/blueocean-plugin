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
import {
    CreatePipeline
} from './creation';

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
            if (child.nodeType !== Node.TEXT_NODE
                    && child.nodeType !== Node.ELEMENT_NODE) {
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

/**
 * Handles navigating to/from run details dialogs...  note this
 * must be done early and can't wait until a modal will mount
 * due to the fact react router will have already changed the
 * the background context.
 */
function handleNavigationChangeToFromModal(prevState, nextState, replace, callback) {
    if (nextState.params.runId && (prevState == null || !prevState.params.runId)) {
        persistModalBackground();
    } else if (!nextState.params.runId) {
         // need to delay this a little to let the route re-render
        setTimeout(discardPersistedBackground, 200);
    }
    callback();
}

export default (
    <Route path="/" component={Dashboard} onChange={handleNavigationChangeToFromModal}>
        <Route path="organizations/:organization" component={OrganizationPipelines}>
            <IndexRedirect to="pipelines" />
            <Route path="pipelines" component={Pipelines} />

            <Route component={PipelinePage}>
                <Route path=":pipeline/branches" component={MultiBranch} />
                <Route path=":pipeline/activity" component={Activity} />
                <Route path=":pipeline/pr" component={PullRequests} />

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
        <Route path="/create-pipeline" component={CreatePipeline} />
        <IndexRedirect to="pipelines" />
    </Route>
);
