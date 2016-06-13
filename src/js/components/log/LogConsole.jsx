// @flow

import React, { Component, PropTypes } from 'react';
import { fetch } from '../fetch';

const { string } = PropTypes;

const INITIAL_RENDER_CHUNK_SIZE = 100;
const INITIAL_RENDER_DELAY = 300;
const RENDER_CHUNK_SIZE = 500;
const RERENDER_DELAY = 17;

type Line = String;

class LogConsole extends Component {

    queuedLines: Array<Line>;
    state: {lines:Array<Line>};

    constructor(props) {
        super(props);

        this.queuedLines = [];

        this.state = {
            lines: []
        };
    }

    componentWillMount() {
        this._processLines(this.props.data);
    }

    componentWillReceiveProps(nextProps) {
        const lines = nextProps.data;

        if (!lines) {
            return;
        }

        this._processLines(lines);
    }

    _processLines(data) {
        let lines = [];

        if (data && data.split) {
            lines = data.split('\n');
        }

        if (lines.length > INITIAL_RENDER_CHUNK_SIZE) {
            // queue up all the lines and grab just the beginning to render for now
            this.queuedLines = lines;
            lines = this.queuedLines.splice(0, INITIAL_RENDER_CHUNK_SIZE);
            setTimeout(() => {
                this._processNextLines();
            }, INITIAL_RENDER_DELAY);
        }

        this.setState({
            lines: lines
        });
    }

    _processNextLines() {
        // grab the next batch of lines and add them to what's already rendered, then re-render
        const renderedLines = this.state.lines;
        const nextLines = this.queuedLines.splice(0, RENDER_CHUNK_SIZE);
        const newLines = renderedLines.concat(nextLines);

        this.setState({
            lines: newLines
        });

        // if more lines are queued, render again
        if (this.queuedLines.length) {
            setTimeout(() => {
                this._processNextLines();
            }, RERENDER_DELAY);
        }
    }

    render() {
        const lines = this.state.lines;

        return (<code
          className="block"
        >
            {lines.map((line, index) => <p key={index}>
                <a key={index} name={index}>{line}</a>
            </p>)}</code>);
    }
}

LogConsole.propTypes = {
    data: string,
    file: string,
    url: string.isRequired,
};

export default fetch(LogConsole, ({ url }, config) => {
    const rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;
