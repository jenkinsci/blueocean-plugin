import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import { Icon } from "@jenkins-cd/react-material-icons";

export class Sheet extends React.Component {
    onClose() {
        const child = React.Children.only(this.props.children);
        child.props.onClose();
    }
    render() {
        const child = React.Children.only(this.props.children);
        return (<div className="sheet">
            <div className="sheet-header">
                {child.props.onClose &&
                    <span className="back-from-sheet" onClick={e => this.onClose()}>
                        <Icon icon="arrow_back"/>
                    </span>
                }
                {child.getTitle && child.getTitle() || child.props.title}
            </div>
            <div className="sheet-body">
                {child}
            </div>
        </div>);
    }
}
export class Sheets extends React.Component {
    componentDidMount() {
        document.addEventListener('keydown', this.escapeListener = e => {
            e = e || window.event;
            if (e.keyCode == 27) {
                this.popTopSheet();
            }
        });
    }
    componentWillUnmount() {
        document.removeEventListener('keydown', this.escapeListener);
    }
    getActiveSheets() {
        const sheetChildren = React.Children.toArray(this.props.children)
            .filter(c => c);
        return sheetChildren;
    }
    popTopSheet() {
        const sheets = this.getActiveSheets();
        const lastSheet = sheets[sheets.length-1];
        if (lastSheet && lastSheet.props.onClose) {
            lastSheet.props.onClose();
        }
    }
    render() {
        if (!this.props.children) {
            return;
        }
        const { transitionDuration = 400, transitionClass = 'sheet' } = this.props;
        const sheetChildren = this.getActiveSheets()
            .map(c => <Sheet key={c.key}>{c}</Sheet>);
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

Sheets.propTypes = {
    children: React.PropTypes.any,
    transitionDuration: React.PropTypes.number,
    transitionClass: React.PropTypes.string,
    className: React.PropTypes.string,
};
