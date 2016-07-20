import React, { Component, PropTypes } from 'react';
import { scrollToHash } from './ScrollToHash';

const { array, bool, string } = PropTypes;

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

    /*
     * This will scroll to the bottom of the console diff
     * React needs the timeout to have the dom ready
     */
    componentDidMount() {
        if (this.props.scrollToBottom) {
            clearTimeout(this.timeouts.scroll);
            this.timeouts.scroll = setTimeout(() => {
                this.updateScroll();
            }, INITIAL_RENDER_DELAY);
        }
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

    /*
     * This will scroll to the bottom of the console diff
     * React needs the timeout to have the dom ready
     */
    componentDidUpdate() {
        if (this.props.scrollToBottom) {
            this.timeouts.scroll = setTimeout(() => {
                this.updateScroll();
            }, INITIAL_RENDER_DELAY);
        }
    }

    componentWillUnmount() {
        this.clearThisTimeout();
    }

    clearThisTimeout() {
        clearTimeout(this.timeouts.scroll);
        clearTimeout(this.timeouts.render);
    }

    // Find the modal view container and adopt the scrollTop to focus the end
    updateScroll() {
        const nodes = document.getElementsByClassName('content');
        const element = nodes[0];
        if (element) {
            element.scrollTop = element.scrollHeight - element.clientHeight;
        }
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
    }

    render() {
        const lines = this.state.lines;
        const { prefix } = this.props;
        if (!lines) {
            return null;
        }

        return (<code
          className="block"
        >
            { lines.map((line, index) => <p key={index + 1} id={`${prefix}log-${index + 1}`}>
                <a key={index + 1} href={`#${prefix || ''}log-${index + 1}`} name={`${prefix}log-${index + 1}`}>{line}</a>
            </p>)}</code>);
    }
}

LogConsole.propTypes = {
    scrollToBottom: bool, // in case of long logs you can scroll to the bottom
    logArray: array,
    prefix: string,
};

export default scrollToHash(LogConsole);

