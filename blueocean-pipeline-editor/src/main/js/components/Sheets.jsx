import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import { Icon } from '@jenkins-cd/design-language';

export class Sheet extends React.Component {
    onClose() {
        const child = React.Children.only(this.props.children);
        child.props.onClose();
    }
    render() {
        const child = React.Children.only(this.props.children);
        return (
            <div className={`sheet ${this.props.active ? 'active' : ''}`}>
                <div className="sheet-header">
                    {child.props.onClose && (
                        <a className="back-from-sheet" onClick={e => this.onClose()}>
                            <Icon icon="NavigationArrowBack" />
                        </a>
                    )}
                    {(child.getTitle && child.getTitle()) || child.props.title}
                </div>
                <div className="sheet-body">{child}</div>
            </div>
        );
    }
}
export class Sheets extends React.Component {
    componentDidMount() {
        document.addEventListener(
            'keydown',
            (this.escapeListener = e => {
                e = e || window.event;
                if (e.keyCode == 27) {
                    this.popTopSheet();
                }
            })
        );
    }
    componentWillUnmount() {
        document.removeEventListener('keydown', this.escapeListener);
    }
    getActiveSheets() {
        const sheetChildren = React.Children.toArray(this.props.children).filter(c => c);
        return sheetChildren;
    }
    popTopSheet() {
        const sheets = this.getActiveSheets();
        const lastSheet = sheets[sheets.length - 1];
        if (lastSheet && lastSheet.props.onClose) {
            lastSheet.props.onClose();
        }
    }
    render() {
        if (!this.props.children) {
            return;
        }
        const { transitionDuration = 400, transitionClass = 'sheet' } = this.props;
        const sheets = this.getActiveSheets();
        const sheetChildren = this.getActiveSheets().map((c, i) => (
            <Sheet active={i == sheets.length - 1} key={c.key}>
                {c}
            </Sheet>
        ));
        return (
            <div className="sheet-container">
                <ReactCSSTransitionGroup
                    transitionName={transitionClass}
                    transitionAppear
                    transitionAppearTimeout={transitionDuration}
                    transitionEnterTimeout={transitionDuration}
                    transitionLeaveTimeout={transitionDuration}
                >
                    {sheetChildren}
                </ReactCSSTransitionGroup>
            </div>
        );
    }
}

Sheet.propTypes = {
    active: React.PropTypes.bool,
};

Sheets.propTypes = {
    children: React.PropTypes.any,
    transitionDuration: React.PropTypes.number,
    transitionClass: React.PropTypes.string,
    className: React.PropTypes.string,
};
