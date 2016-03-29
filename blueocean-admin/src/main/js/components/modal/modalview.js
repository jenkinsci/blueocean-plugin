import React, {Component, PropTypes} from 'react';
import defaultStyles from './styles';
import Header from './header';
import Body from './body';

class ModalView extends Component {

    constructor(props) {
        super(props);
        this.state = {isVisible: props.isVisible || false};
    }

    componentWillUpdate(nextProps, nextState) {

        const {isVisible} = this.state;
        const {beforeOpen, beforeClose} = this.props;

        if (nextState.isVisible && !isVisible && beforeOpen) {
            beforeOpen();
        }

        if (!nextState.isVisible && isVisible && beforeClose) {
            beforeClose();
        }
    }

    componentDidUpdate(prevProps, prevState) {

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

    getParts(children, part) {
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
                ...rest,
                },
            } = this;

        const head = this.getParts(children, 'Header');
        const body = this.getParts(children, 'Body');

        if (rest.showOverlay) {
            overlay = (<div
                onClick={() => this.onOverlayClicked()}
                style={overlayStyles}
                className="overlayStyles">
            </div>);
        }

        return ( <section className="modalview">
                {overlay}
                <div style={dialogStyles} className="dialog">
                    <div style={headerStyle} className="header">
                        <a onClick={() => this.hide()}
                           role="button"
                           style={closeButtonStyle}>&times;</a>
                        {
                            head && head[0] ? head : <Header {...titleStyle} {...rest}/>
                        }
                    </div>
                    <div style={contentStyle} className="content">
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

    title: PropTypes.string,
    body: PropTypes.string,
    children: PropTypes.object,

    onOverlayClicked: PropTypes.func,
    hideOnOverlayClicked: PropTypes.bool,
    showOverlay: PropTypes.bool,
    isVisible: PropTypes.bool,

    styles: React.PropTypes.oneOfType([
        PropTypes.shape({
            closeButtonStyle: PropTypes.object,
            dialogStyles: PropTypes.object,
            overlayStyles: PropTypes.object,
            titleStyle: PropTypes.object
        }),
        PropTypes.bool,
    ]),

};

ModalView.defaultProps = {
    showOverlay: true,
    hideOnOverlayClicked: false
};

export {
    ModalView as default,
    Header,
    Body,
    defaultStyles,
};
