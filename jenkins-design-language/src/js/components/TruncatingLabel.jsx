// @flow

import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';

//--------------------------------------
//  Safety constants
//--------------------------------------

const MINLENGTH = 5; // Minimum size of cut-down text
const MAXLOOPS = 50; // Max no of iterations attempting to find the correct size text

//--------------------------------------
//  Flow types
//--------------------------------------

// Render lifecycle
const RS_MEASURE = 'measure'; // Mounted, text/props changed, measurement needed.
const RS_FLUID = 'fluid'; // Text too big, in the process of trimming it down
const RS_STABLE = 'stable'; // Done measuring, until props change

export const RenderStateValues = {
    [RS_MEASURE]: RS_MEASURE,
    [RS_FLUID]: RS_FLUID,
    [RS_STABLE]: RS_STABLE,
};

type RenderState = $Keys<typeof RenderStateValues>;

// react.Component.props
type Props = {
    children?: string,
    style?: Object,
    className?: string,
};

/**
 * Multi-line label that will truncate with ellipses
 *
 * Use with a set width + height (or maxWidth / maxHeight) to get any use from it :D
 *
 * @deprecated Don't use this, the latest version exists in the ux-widgets repo alongside the updated PipelineGraph
 */
export class TruncatingLabel extends Component {
    //--------------------------------------
    //  Component state / lifecycle
    //--------------------------------------

    props: Props;

    completeText: string; // Unabridged plain text content
    innerText: string; // Current innerText of element - includes possible ellipses
    renderState: RenderState; // Internal rendering lifecycle state
    checkSizeRequest: number; // window.requestAnimationFrame handle

    //--------------------------------------
    //  Binary search state
    //--------------------------------------

    textCutoffLength: number; // Last count used to truncate completeText
    longestGood: number; // Length of the longest truncated text that fits
    shortestBad: number; // Length of the shortest truncated text that does not fit
    loopCount: number; // to avoid infinite iteration

    //--------------------------------------
    //  React Lifecycle
    //--------------------------------------

    componentWillMount() {
        this.handleProps(this.props);
    }

    componentWillReceiveProps(nextProps: Props) {
        this.handleProps(nextProps);
    }

    componentDidMount() {
        this.invalidateSize();
    }

    componentDidUpdate() {
        this.invalidateSize();
    }

    componentWillUnmount() {
        if (this.checkSizeRequest) {
            cancelAnimationFrame(this.checkSizeRequest);
            this.checkSizeRequest = 0;
        }
    }

    //--------------------------------------
    //  Render
    //--------------------------------------

    render() {
        const { style = {}, className = '' } = this.props;

        const mergedStyle = {
            overflow: 'hidden',
            wordWrap: 'break-word',
            ...style,
        };

        if (this.renderState !== RS_STABLE) {
            mergedStyle.opacity = 0;
        }

        return (
            <div style={mergedStyle} className={'TruncatingLabel ' + className} title={this.innerText}>
                {this.innerText}
            </div>
        );
    }

    //--------------------------------------
    //  Internal Rendering Lifecycle
    //--------------------------------------

    handleProps(props: Props) {
        const { children = '' } = props;

        if (typeof children === 'string') {
            this.completeText = children;
        } else if (children === null || children === false) {
            this.completeText = ''; // Assume content has been boolean'd out
        } else {
            console.warn('TruncatingLabel - Label children must be string but is', typeof children, children);
            this.completeText = 'Contents must be string';
        }

        this.renderState = RS_MEASURE;
        this.innerText = this.completeText;
        this.loopCount = 0;
        this.longestGood = MINLENGTH;
        this.shortestBad = this.innerText.length;
    }

    invalidateSize() {
        if (!this.checkSizeRequest) {
            this.checkSizeRequest = requestAnimationFrame(() => this.checkSize());
        }
    }

    checkSize() {
        this.checkSizeRequest = 0;

        if (this.renderState === RS_STABLE) {
            return; // Nothing to check, no more checks to schedule
        }

        const thisElement = ReactDOM.findDOMNode(this);
        const { scrollHeight, clientHeight, scrollWidth, clientWidth } = thisElement;

        const tooBig = scrollHeight > clientHeight || scrollWidth > clientWidth;

        if (this.renderState === RS_MEASURE) {
            // First measurement since mount / props changed

            if (tooBig) {
                this.renderState = RS_FLUID;

                // Set initial params for binary search of length
                this.longestGood = MINLENGTH;
                this.textCutoffLength = this.shortestBad = this.innerText.length;
            } else {
                this.renderState = RS_STABLE;
                this.forceUpdate(); // Re-render via react so it can update the alpha
            }
        }

        if (this.renderState === RS_FLUID) {
            this.loopCount++;

            const lastLength = this.textCutoffLength;

            let keepMeasuring;

            if (this.loopCount >= MAXLOOPS) {
                // This really shouldn't happen!
                console.error('TruncatingLabel - TOO MANY LOOPS');
                keepMeasuring = false;
            } else if (lastLength <= MINLENGTH) {
                keepMeasuring = false;
            } else if (Math.abs(this.shortestBad - this.longestGood) < 2) {
                // We're done searching, hoorays!
                keepMeasuring = false;
            } else {
                // Update search space
                if (tooBig) {
                    this.shortestBad = Math.min(this.shortestBad, lastLength);
                } else {
                    this.longestGood = Math.max(this.longestGood, lastLength);
                }

                // Calculate the next length and update the text
                this.textCutoffLength = Math.floor((this.longestGood + this.shortestBad) / 2);
                this.innerText = this.completeText.substr(0, this.textCutoffLength) + '…';

                // Bypass react's render loop during the "fluid" state for performance
                thisElement.innerText = this.innerText;
                keepMeasuring = true;
            }

            if (keepMeasuring) {
                this.invalidateSize();
            } else {
                this.renderState = RS_STABLE;
                this.forceUpdate(); // Re-render via react so it knows about updated alpha and final content
            }
        }
    }
}

TruncatingLabel.propTypes = {
    children: PropTypes.string,
    style: PropTypes.object,
    className: PropTypes.string,
};
