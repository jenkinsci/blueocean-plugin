import React, { Component, PropTypes } from 'react';
import { scrollHelper } from './ScrollHelper';

const INITIAL_RENDER_CHUNK_SIZE = 100;
const INITIAL_RENDER_DELAY = 300;
const RENDER_CHUNK_SIZE = 500;
const RERENDER_DELAY = 17;


export class LogConsole extends Component {

    constructor(props) {
        super(props);

        this.queuedLines = [];
        this.state = {
            lines: [],
        };
        // we have different timeouts in this component, each will take its own workspace
        this.timeouts = {};
    }

    componentWillMount() {
        this._processLines(this.props.logArray);
    }


    // componentWillReceiveProps does not return anything and return null is an early out, so disable lint complaining
    componentWillReceiveProps(nextProps) { // eslint-disable-line
        const newArray = nextProps.logArray;
        // we only want to update if we have an array and if it is new
        if (!newArray || (newArray && newArray === this.props.logArray)) {
            return null;
        }
        // if have a new logArray, simply add it to the queue and wait for next tick
        this.queuedLines = this.queuedLines.concat(newArray);
        clearTimeout(this.timeouts.render);
        this.timeouts.render = setTimeout(() => {
            this._processNextLines();
        }, INITIAL_RENDER_DELAY);
    }

    componentWillUnmount() {
        this.clearThisTimeout();
    }

    clearThisTimeout() {
        clearTimeout(this.timeouts.scroll);
        clearTimeout(this.timeouts.render);
    }

    // initial method to create lines to render
    _processLines(lines) {
        let newLines = lines;
        if (newLines && newLines.length > INITIAL_RENDER_CHUNK_SIZE) {
            // queue up all the lines and grab just the beginning to render for now
            this.queuedLines = this.queuedLines.concat(newLines);
            newLines = this.queuedLines.splice(0, INITIAL_RENDER_CHUNK_SIZE);
            clearTimeout(this.timeouts.render);
            this.timeouts.render = setTimeout(() => {
                this._processNextLines();
            }, INITIAL_RENDER_DELAY);
        } else {
            this.scroll();
        }

        this.setState({
            lines: newLines,
        });
    }

    // generic method to render more lines if so
    _processNextLines() {
        // grab the next batch of lines and add them to what's already rendered, then re-render
        const renderedLines = this.state.lines || [];
        const nextLines = this.queuedLines.splice(0, RENDER_CHUNK_SIZE);
        const newLines = renderedLines.concat(nextLines);

        this.setState({
            lines: newLines,
        });

        // if more lines are queued, render again
        if (this.queuedLines.length) {
            clearTimeout(this.timeouts.render);
            this.timeouts.render = setTimeout(() => {
                this._processNextLines();
            }, RERENDER_DELAY);
        }
        this.scroll();
    }

    scroll() {
        const anchorName = window.location.hash;
        const stepReg = /log-([0-9]{1,})$/;
        const match = stepReg.exec(anchorName);
        /*
         * This will scroll to the bottom of the console diff
         * React needs the timeout to have the dom ready
         */
        if (this.props.scrollToBottom && !match) {
            this.timeouts.scroll = setTimeout(() => this.props.scrollBottom(), RERENDER_DELAY + 1);
        } else if (match) {
            // we need to scroll to a certain line now
            this.timeouts.scroll = this.props.scrollToAnchorTimeOut(RERENDER_DELAY + 1);
        }
    }

    render() {
        const lines = this.state.lines;
        const { prefix = '', hasMore = false } = this.props; // if hasMore true then show link to full log
        if (!lines) {
            return null;
        }

        return (<code
          className="block"
        >
            { hasMore && <p key={0} id={`${prefix}log-${0}`}>
                <a
                  key={0}
                  href={`?start=0#${prefix || ''}log-${1}`}
                >
                Full Log
            </a>
            </p>}
            { lines.map((line, index) => <p key={index + 1} id={`${prefix}log-${index + 1}`}>
                <a
                  key={index + 1}
                  href={`#${prefix || ''}log-${index + 1}`}
                  name={`${prefix}log-${index + 1}`}
                >{line}
                </a>
            </p>)}</code>);
    }
}

const { array, bool, string, func } = PropTypes;
LogConsole.propTypes = {
    scrollToBottom: bool, // in case of long logs you can scroll to the bottom
    logArray: array,
    scrollToAnchorTimeOut: func,
    scrollBottom: func,
    prefix: string,
    hasMore: bool,
};

export default scrollHelper(LogConsole);

