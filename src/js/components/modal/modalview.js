// @flow

import React, {Component, PropTypes} from 'react';
import defaultStyles from './styles';
import Header from './header';
import Body from './body';

// Typedefs
type Props = {
    afterClose?: () => void,
    afterOpen?: () => void,
    beforeClose?: () => void,
    beforeOpen?: () => void,
    body?: string,
    children?: any, // We can refine this once the Flow team does :)
    hideOnOverlayClicked?: boolean,
    isVisible?: boolean,
    onOverlayClicked?: () => void,
    showOverlay?: boolean,
    styles?:
        {
            closeButtonStyle: Object,
            dialogStyles: Object,
            overlayStyles: Object,
            titleStyle: Object
        } | boolean,
    result?: string,
    title?: string,
    ignoreEscapeKey?: boolean
};

type State = {
    isVisible: boolean
}

class ModalView extends Component {

    state: State;

    static defaultProps: Props = {
        styles: false,
        showOverlay: true,
        hideOnOverlayClicked: false,
        ignoreEscapeKey: false
    };

    constructor(props: Props) {
        super(props);
        this.state = {isVisible: props.isVisible || false};
    }

    componentWillMount() {
        document.addEventListener("keydown", this._handleKeys, false);
    }

    componentWillUnmount() {
        document.removeEventListener("keydown", this._handleKeys, false);
    }

    _handleKeys = (event) => {
        const { ignoreEscapeKey } = this.props;

        if (!ignoreEscapeKey && event.keyCode == 27) {
            this.hide();
        }
    }

    componentWillUpdate(nextProps: Props, nextState: State) {

        const {isVisible} = this.state;
        const {beforeOpen, beforeClose} = this.props;

        if (nextState.isVisible && !isVisible && beforeOpen) {
            beforeOpen();
        }

        if (!nextState.isVisible && isVisible && beforeClose) {
            beforeClose();
        }
    }

    componentDidUpdate(prevProps: Props, prevState: State) {

        const {isVisible} = this.state;
        const {afterOpen, afterClose} = this.props;

        if (!prevState.isVisible && isVisible && afterOpen) {
            afterOpen();
        }

        if (prevState.isVisible && !isVisible && afterClose) {
            afterClose();
        }
    }

    show() {
        this.setState({isVisible: true});
    }

    hide() {
        this.setState({isVisible: false});
    }

    onOverlayClicked() {
        const { hideOnOverlayClicked, onOverlayClicked } = this.props;

        if (hideOnOverlayClicked) {
            this.hide();
        }

        if (onOverlayClicked) {
            onOverlayClicked();
        }
    }

    getStyles() {
        const styleArray = [
            'dialogStyles',
            'overlayStyles',
            'closeButtonStyle',
            'titleStyle',
            'headerStyle',
            'contentStyle'
        ];
        const {styles = {}} = this.props;
        const noStyle = !styles;

        const returnObject = {};

        styleArray.map((item) => {
            returnObject[item] = !noStyle ? Object.assign({}, defaultStyles[item], styles[item]) : {};
        });

        return returnObject;

    }

    getParts(children: Array<any>, part: string) {
        return React.Children.map(children, (child) => {
            if (child.type instanceof Function && child.type.name === part) {
                return child;
            }
        });
    }

    render() {
        const {isVisible} = this.state;
        //early out
        if (!isVisible) {
            return null;
        }

        let overlay;

        const {
            dialogStyles,
            overlayStyles,
            closeButtonStyle,
            titleStyle,
            contentStyle,
            headerStyle,
            } =  this.getStyles();

        const {
            props: {
                children,
                result = 'info',
                ...rest,
                },
            } = this;

        const head = this.getParts(children, 'Header');
        const body = this.getParts(children, 'Body');

        if (rest.showOverlay) {
            overlay = (<div
                className="overlayStyles"
                onClick={() => this.onOverlayClicked()}
                style={overlayStyles}
            >
            </div>);
        }


        return ( <section className="modalview">
                {overlay}
                <div className="dialog" style={dialogStyles}>
                    <div className={`header ${result.toLowerCase()}`} style={headerStyle}>
                        <a onClick={() => this.hide()}
                           role="button"
                           className="closeButton"
                           style={closeButtonStyle}>&times;</a>
                        {
                            head && head[0] ? head : <Header {...titleStyle} {...rest}/>
                        }
                    </div>
                    <div className="content" style={contentStyle}>
                        {
                            body ? body[0] : <Body {...rest}>{children}</Body>
                        }
                    </div>

                </div>
            </section>
        );
    }
}

ModalView.displayName = 'ModalView';

ModalView.propTypes = {
    afterClose: PropTypes.func,
    afterOpen: PropTypes.func,
    beforeClose: PropTypes.func,
    beforeOpen: PropTypes.func,
    body: PropTypes.string,
    children: PropTypes.node,
    hideOnOverlayClicked: PropTypes.bool,
    isVisible: PropTypes.bool,
    onOverlayClicked: PropTypes.func,
    showOverlay: PropTypes.bool,
    styles: React.PropTypes.oneOfType([
        PropTypes.shape({
            closeButtonStyle: PropTypes.object,
            dialogStyles: PropTypes.object,
            overlayStyles: PropTypes.object,
            titleStyle: PropTypes.object
        }),
        PropTypes.bool,
    ]),
    result: PropTypes.string,
    title: PropTypes.string,
    ignoreEscapeKey: PropTypes.bool
};

export {
    Body as ModalBody,
    defaultStyles as ModalStyles,
    ModalView,
    Header as ModalHeader,
};
