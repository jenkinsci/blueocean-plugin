import React, {Component} from 'react';

export class Table extends Component {
    render() {
        return <table>{this.props.children}</table>;
    }
}
