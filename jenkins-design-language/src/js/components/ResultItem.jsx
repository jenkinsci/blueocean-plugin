// @flow

import React, { Component, PropTypes } from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import Linkify from 'linkifyjs/react';

import { StatusIndicator, decodeResultValue } from './status/StatusIndicator';
import { getGlyphFor } from './status/SvgStatus';

import type { Result } from './status/StatusIndicator';

type State = {
    resultClean: Result,
    statusGlyph: ?ReactChildren,
    expanded: boolean
};

type Props = {
    result: Result,
    label: String,
    extraInfo: ?String | Object,
    data: ?any,
    onExpand: (data: ?any, event: ?Event) => void,
    onCollapse: (data: ?any, event: ?Event) => void,
    expanded: ?boolean,
};

export class ResultItem extends Component {

    state:State;

    constructor(props: Props) {
        super(props);
        this.state = {
            resultClean: StatusIndicator.validResultValues.unknown,
            statusGlyph: null,
            expanded: props.expanded || false,
        };
    }

    componentWillMount() {
        this.handleProps(this.props);
    }

    componentWillReceiveProps(nextProps: Props) {
        this.handleProps(nextProps);
    }

    handleProps(props: Props) {
        const resultClean = decodeResultValue(props.result);
        if (resultClean !== this.state.resultClean) {
            const statusGlyph = getGlyphFor(resultClean);
            this.setState({resultClean, statusGlyph});
        }
        // check whether we want to change the state or whether we already are in the correct state
        if (props.expanded !== this.state.expanded) {
            this.toggleExpanded();
        }
    }

    toggleExpanded: Function = (e) => {
        const selection = window.getSelection ? window.getSelection() : false;
        const selected = selection && selection.toString();
        if (this.props.children && !selected) {
            const expanded = !this.state.expanded;

            this.setState({expanded}, () => {
                const {data, onExpand, onCollapse} = this.props;
                // Data is arbitrary, set by parent

                if (onExpand && expanded) {
                    onExpand(data, e);
                }

                if (onCollapse && !expanded) {
                    onCollapse(data, e);
                }
            });
        }
    };

    urlClicked = (e: Event) => {
        console.log('urlClicked');
        e.stopPropagation();
    };

    render() {
        const { label, extraInfo } = this.props;
        const { resultClean, statusGlyph } = this.state;

        const hasChildren = !!this.props.children;
        const expanded = this.state.expanded && hasChildren;

        const classes = ['result-item', resultClean];

        if (expanded) {
            classes.push('expanded');
        }

        const outerClassName = classes.join(' ');
        const iconClassName = `result-item-icon result-bg ${resultClean}`;

        return (
            <div className={outerClassName}>
                <div className="result-item-head" onClick={this.toggleExpanded}>
                    <span className={iconClassName}>
                        <svg width="28" height="34">
                            <g transform="translate(14 18)" className="result-status-glyph">{statusGlyph}</g>
                        </svg>
                    </span>
                    <span className="result-item-title">
                        <Expando expanded={expanded} disabled={!hasChildren}/>
                        <span className="result-item-label">
                            <Linkify options={{attributes: {onClick: this.urlClicked}}}>{label}</Linkify>
                        </span>
                        <span className="result-item-extra-info">
                            {extraInfo}
                        </span>
                    </span>
                </div>
                <ReactCSSTransitionGroup transitionName="slide-down"
                                         transitionAppear
                                         transitionAppearTimeout={300}
                                         transitionEnterTimeout={300}
                                         transitionLeaveTimeout={300}>{
                    expanded ? <div className="result-item-children" key="k">{this.props.children}</div>
                        : null
                }</ReactCSSTransitionGroup>
            </div>
        );
    }
}

ResultItem.propTypes = {
    result: PropTypes.oneOf(Object.keys(StatusIndicator.validResultValues)),
    label: PropTypes.string,
    extraInfo: PropTypes.oneOfType([PropTypes.string, PropTypes.object]),
    data: PropTypes.any, // Whatever you want, will be sent back to listeners
    onExpand: PropTypes.func, // f(data:*, originalEvent:?event)
    onCollapse: PropTypes.func, // f(data:*, originalEvent:?event)
    children: PropTypes.node,
};

// We can extract this into an exported component if we need it elsewhere
class Expando extends Component {
    render() {

        const classes = ['result-item-expando'];

        if (this.props.expanded) {
            classes.push('expanded');
        }

        if (this.props.disabled) {
            classes.push('disabled');
        }

        const outerClassName = classes.join(' ');
        return (
            <svg width="28" height="24" className={outerClassName}>
                <g transform="translate(14 12)">
                    <g className="expando-glyph">
                        <polygon points="-1.7,-5 3.3,0 -1.7,5 -2.9,3.8 1,0 -2.9,-3.8"/>
                    </g>
                </g>
            </svg>
        );
    }
}

Expando.propTypes = {
    expanded: PropTypes.bool,
    disabled: PropTypes.bool,
};
