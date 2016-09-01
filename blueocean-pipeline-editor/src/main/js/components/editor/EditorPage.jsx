// @flow

import React, { Component, PropTypes } from 'react';

type Props = {
    title?: string,
    children: any,
    style?: ?Object
};

type State = {};

type DefaultProps = typeof EditorPage.defaultProps;

export class EditorPage extends Component<DefaultProps, Props, State> {

    static defaultProps = {
        children: (null: any)
    };

    //static propTypes = {};
    // TODO: React proptypes ^^^

    state:State;

    render() {

        let {title = "Create Pipeline", style} = this.props;

        return (
            <div className="editor-page-outer" style={style}>
                <div className="editor-page-header">
                    <h3>{ title }</h3>
                    <div className="editor-page-header-controls">
                        <button className="btn-secondary inverse">Discard Changes</button>
                        <button className="btn inverse">Save</button>
                    </div>
                </div>
                {this.props.children}
            </div>
        );
    }
}

export default EditorPage;
