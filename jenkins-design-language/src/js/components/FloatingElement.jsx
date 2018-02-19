// @flow

import React, { Component, PropTypes } from 'react';

//--------------------------------------------------------------------------
//
//  Constants / types
//
//--------------------------------------------------------------------------

const pollingInterval = 50; // ms
const fixedAnimationDuration = 500; // ms

const stateInit = 'init'; // Created, not yet correctly positioned
const stateStable = 'stable'; // Correctly positioned, all good
const stateMoving = 'moving'; // Moving to new correct position

const lifecycleStates = {
    init: 'init',
    stable: 'stable',
    moving: 'moving'
};

export type LifecycleState = $Keys<typeof lifecycleStates>;

type Props = {
    positionFunction?: Function,
};

//--------------------------------------------------------------------------
//
//  Helpers
//
//--------------------------------------------------------------------------



//--------------------------------------
//  Animation easing
//--------------------------------------

// t: current time, b: beginning value, c: change In value, d: duration
// Based on https://github.com/danro/jquery-easing/

const tween = easeInOutCubic;

function easeInOutCubic(t, b, c, d) {
    if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
    return c / 2 * ((t -= 2) * t * t + 2) + b;
}

//--------------------------------------------------------------------------
//
//  Main Component
//
//--------------------------------------------------------------------------

export class FloatingElement extends Component {

    //--------------------------------------
    //  Internal State
    //--------------------------------------

    lifecycleState:LifecycleState = stateInit;
    currentLeft:number = 0;
    currentTop:number = 0;

    //--------------------------------------
    //  Animation Progress
    //--------------------------------------

    animationStart:number = 0; // ms
    animationDuration:number = 0; // ms
    goalLeft:number = 0; // Goal-point for animation
    goalTop:number = 0; // Goal-point for animation

    //--------------------------------------
    //  Last-known measurements
    //--------------------------------------

    selfWidth:number = 0;
    selfHeight:number = 0;

    targetWidth:number = 0;
    targetHeight:number = 0;
    targetLeft:number = 0;
    targetTop:number = 0;

    viewportWidth:number = 0;
    viewportHeight:number = 0;

    //--------------------------------------
    //  Positioning Loop
    //--------------------------------------

    positioningValid:boolean = false;
    validatePositioningScheduled:boolean = false;
    checkDOMDependenciesScheduled:boolean = false;
    pollingTimeout:any = null;

    /**
     Checks the measurements of things we depend on, and invalidate positioning
     if they've changed. This is designed to be called often, from a timer or
     event listeners.

     Can be called at any time, and will schedule itself into an animation
     frame.
     */
    checkDOMDependencies() {
        if (this.positioningValid
          && !this.validatePositioningScheduled
          && !this.checkDOMDependenciesScheduled) {

            this.checkDOMDependenciesScheduled = true;
            window.requestAnimationFrame(() => {
                this.checkDOMDependenciesScheduled = false;
                if (this.measureDOMNodes()) {
                    this.invalidatePositioning();
                }
            });
        }
    }

    /**
     Call when you want to make sure to re-calculate the positioning.

     Can be called at any time, and will schedule itself into an animation
     frame.
     */
    invalidatePositioning() {
        if (this.positioningValid) {
            this.positioningValid = false;
        }

        if (!this.validatePositioningScheduled) {
            this.validatePositioningScheduled = true;

            window.requestAnimationFrame(() => {
                this.validatePositioningScheduled = false;
                this.validatePositioning();
            });
        }
    }

    /**
     Check for invalidated positioning, and re-calculate / move if necessary

     Should only be called via window.requestAnimationFrame().
     */
    validatePositioning() {
        if (!this.positioningValid) {
            this.calculateAndSetPopupPosition();
        }
    }

    /**
     Locate and measure the DOM nodes of target and self.
     Returns true if there's a change.

     Should only be called via window.requestAnimationFrame().
     */
    measureDOMNodes() {

        let newSelfWidth = 0;
        let newSelfHeight = 0;

        let newTargetWidth = 0;
        let newTargetHeight = 0;
        let newTargetLeft = 0;
        let newTargetTop = 0;

        let newViewportWidth = 0;
        let newViewportHeight = 0;

        // Measure self
        const selfNode = this.refs.wrapper;
        if (selfNode) {
            newSelfWidth = selfNode.offsetWidth;
            newSelfHeight = selfNode.offsetHeight;
        }

        // Measure and locate target element
        const targetNode = this.props.targetElement;

        if (targetNode) {
            newTargetWidth = targetNode.offsetWidth;
            newTargetHeight = targetNode.offsetHeight;

            // Calculate total position (only offsetParent chain)
            let node = targetNode;
            while (node) {
                newTargetLeft += node.offsetLeft;
                newTargetTop += node.offsetTop;

                node = node.offsetParent;
            }

            // Calculate total scroll offset (can be in any ancestor)
            node = targetNode.parentElement;
            while (node) {

                if (node.scrollLeft) {
                    newTargetLeft -= node.scrollLeft;
                }
                if (node.scrollTop) {
                    newTargetTop -= node.scrollTop;
                }

                node = node.parentElement;
            }
        }

        // Viewport
        newViewportWidth = window.innerWidth;
        newViewportHeight = window.innerHeight;

        const changed = newSelfWidth !== this.selfWidth ||
          newSelfHeight !== this.selfHeight ||
          newTargetWidth !== this.targetWidth ||
          newTargetHeight !== this.targetHeight ||
          newTargetLeft !== this.targetLeft ||
          newTargetTop !== this.targetTop ||
          newViewportWidth !== this.viewportWidth ||
          newViewportHeight !== this.viewportHeight;

        if (changed) {
            this.selfWidth = newSelfWidth;
            this.selfHeight = newSelfHeight;
            this.targetWidth = newTargetWidth;
            this.targetHeight = newTargetHeight;
            this.targetLeft = newTargetLeft;
            this.targetTop = newTargetTop;
            this.viewportWidth = newViewportWidth;
            this.viewportHeight = newViewportHeight;
        }

        return changed;
    }

    /**
     * Set the popover's initial position (off-screen)
     * Avoids a Safari/IE bug where content would flash at 0/0 before being positioned.
     */
    setInitialPosition() {
        this.movePopover(-9999,-9999);
        // leave in the init state so the eventual repositioning will happen immediately (no anim)
        this.lifecycleState = lifecycleStates.init;
    }

    /**
     Calculate popover position based on its dimensions, preferred position,
     and the dimensions and position of the anchor. If there's a change,
     invokes movePopover to move the popover.

     Should only be called via window.requestAnimationFrame().
     */
    calculateAndSetPopupPosition() {

        this.measureDOMNodes();

        const {
          selfWidth,
          selfHeight,
          targetWidth,
          targetHeight,
          targetLeft,
          targetTop,
          viewportWidth,
          viewportHeight
        } = this;

        const newPositions = this.props.positionFunction(
          selfWidth,
          selfHeight,
          targetWidth,
          targetHeight,
          targetLeft,
          targetTop,
          viewportWidth,
          viewportHeight
        );

        const { newLeft, newTop } = newPositions;

        this.movePopover(newLeft, newTop);
        this.positioningValid = true;
    }

    /**
     Move the popover div to its new location. May begin an animation
     depending on current state
     */
    movePopover(newLeft:number, newTop:number) {
        const node = this.refs.wrapper;

        if (!node) {
            return;
        }

        switch (this.lifecycleState) {
            case stateInit:
                // Wishlist: Add an initial "appear" animation?
                this.lifecycleState = stateStable;
                this.currentLeft = newLeft;
                this.currentTop = newTop;
                node.style.left = this.currentLeft + 'px';
                node.style.top = this.currentTop + 'px';
                break;

            case stateStable:
                this.lifecycleState = stateMoving;
                this.animationStart = performance.now();
                this.animationDuration = fixedAnimationDuration;
                this.goalLeft = newLeft;
                this.goalTop = newTop;
                requestAnimationFrame(this.movePopoverAnimationFrame);
                break;
            case stateMoving:
                // Wishlist: try to merge old and new animations if moving by
                // calculating a new virtual start position?
                this.goalLeft = newLeft;
                this.goalTop = newTop;
                requestAnimationFrame(this.movePopoverAnimationFrame);
                break;
        }
    }

    /**
     Schedule a future check of DOM sizes and positions to see if we need to
     move our popover. Will reschedule itself afterwards.
     */
    startPollTimeout() {
        if (this.pollingTimeout) {
            clearTimeout(this.pollingTimeout);
        }

        this.pollingTimeout = setTimeout(
          () => {
              this.pollingTimeout = null;
              this.checkDOMDependencies();
              this.startPollTimeout();
          }, pollingInterval);
    }

    //--------------------------------------
    //  Animation
    //--------------------------------------

    /**
     Renders a frame in the process of animating from currentLeft / currentTop
     to goalLeft / goalTop
     */
    movePopoverAnimationFrame = (now: number) => {
        const node = this.refs.wrapper;
        const {
          lifecycleState,
          currentLeft,
          currentTop,
          animationStart,
          animationDuration,
          goalLeft,
          goalTop
        } = this;

        if (!node || lifecycleState !== stateMoving) {
            return; // Nothing to do here
        }

        // If we're done, make sure to set correct location, stop animation
        if (now >= animationStart + animationDuration) {
            this.currentLeft = goalLeft;
            this.currentTop = goalTop;
            this.animationStart = 0;
            this.animationDuration = 0;
            this.goalLeft = 0;
            this.goalTop = 0;
            this.lifecycleState = stateStable;

            node.style.left = goalLeft + 'px';
            node.style.top = goalTop + 'px';

            return;
        }

        let newLeft = 0,
          newTop = 0;
        const time = now - animationStart;

        newLeft = Math.round(tween(
          time,
          currentLeft,
          goalLeft - currentLeft,
          animationDuration));

        newTop = Math.round(tween(
          time,
          currentTop,
          goalTop - currentTop,
          animationDuration));

        // Position the node, update state
        node.style.left = newLeft + 'px';
        node.style.top = newTop + 'px';

        // Request another frame
        window.requestAnimationFrame(this.movePopoverAnimationFrame);
    };

    //--------------------------------------
    //  React Lifecycle
    //--------------------------------------

    render() {
        const { children, style } = this.props;
        const wrapperStyle = {
            ...style,
            left: this.currentLeft + 'px',
            top: this.currentTop + 'px'
        };
        return (
          <div className="FloatingElement">
              <div ref="wrapper" className="FloatingElement-wrapper" style={wrapperStyle}>
                  {children}
              </div>
          </div>
        );
    }

    componentWillReceiveProps(nextProps:Props) {
        if (nextProps.positionFunction !== this.props.positionFunction
          || nextProps.targetElement !== this.props.targetElement) {
            this.invalidatePositioning();
        }
    }

    componentDidMount() {
        this.componentRendered();
    }

    componentDidUpdate() {
        this.componentRendered();
    }

    componentRendered() {
        this.setInitialPosition();
        // As soon as possible, we need to re-calculate our position
        this.validatePositioningScheduled = true;
        window.requestAnimationFrame(() => {
            this.validatePositioningScheduled = false;
            this.validatePositioning();
        });

        // Start the periodic timeout to check dom measurements
        this.startPollTimeout();
    }

    componentWillUnmount() {
        if (this.pollingTimeout) {
            clearTimeout(this.pollingTimeout);
            this.pollingTimeout = null;
        }
    }

    static propTypes = {
        targetElement: PropTypes.object,
        positionFunction: PropTypes.func,
        style: PropTypes.object,
        children: PropTypes.node
    }
}
