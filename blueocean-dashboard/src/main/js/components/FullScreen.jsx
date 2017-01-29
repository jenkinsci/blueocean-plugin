import React, {Component, PropTypes} from 'react';

import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

const transitionClass = "expand-in";
const transitionDuration = 150;

// FIXME: Move styles to css once this is bedded down and moved to JDL
const outerStyles = {
    position: "fixed",
    left: 0,
    top: 0,
    bottom: 0,
    right: 0 ,
    zIndex: 75
};

const defaultContainerStyles = {
    background: "white",
    width: "100%",
    height: "100%"
};

export class FullScreen extends Component {
    constructor(props) {
        super(props);

        // If nothing set, default to true
        const isVisible = "isVisible" in props ? props.isVisible : true;

        this.transitionTimeout = undefined;
    }

    componentWillReceiveProps(newProps) {

        const {isVisible} = newProps;

        if (isVisible != this.props.isVisible) {
            clearTimeout(this.transitionTimeout);

            this.transitionTimeout = setTimeout(() => {
                this.transitionTimeout = undefined;
                this.forceUpdate();
                if (this.props.afterClose) {
                    this.props.afterClose();
                }
            }, transitionDuration);
        }
    }

    render() {

        /*
            The top div (FullScreen) escapes the containing document flow, the inner one (FullScreen-contents)
            wraps props.children in a single node for the sake of the animation, and defaults to width/height: 100%
            and background: white
        */

        const {children, style, isVisible} = this.props;

        // If transitionTimeout not null, we're still fading in/out
        if (!isVisible && !this.transitionTimeout) {
            return null;
        }

        // We apply any styles that are passed in to the div that wraps the children.
        const containerStyles = {...defaultContainerStyles, ...style};

        const wrappedChildren = isVisible && (
                <div className="FullScreen-contents" style={containerStyles}>
                    {children}
                </div>
            );

        return (
            <div className="FullScreen" style={outerStyles}>
                <ReactCSSTransitionGroup
                    transitionName={transitionClass}
                    transitionAppear
                    transitionAppearTimeout={transitionDuration}
                    transitionEnterTimeout={transitionDuration}
                    transitionLeaveTimeout={transitionDuration}
                >
                    { wrappedChildren  }
                </ReactCSSTransitionGroup>
            </div>
        );
    }
}

FullScreen.propTypes = {
    isVisible: PropTypes.bool,
    children: PropTypes.node,
    style: PropTypes.object,
    afterClose: PropTypes.func,
};

FullScreen.defaultProps = {
    isVisible: true
};
