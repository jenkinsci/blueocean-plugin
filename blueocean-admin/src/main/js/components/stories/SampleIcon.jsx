import React, { Component, PropTypes } from 'react';

import {MorphIcon} from 'react-morph-material-icons-clone';

export default class SampleIcon extends Component {
    constructor(props) {
        super(props);
        this.state = {
            iconScope: ['done_all', 'favorite_outline', 'explore', 'translate', 'open_with', 'perm_media', 'new_releases'],
            fills: ['#4CAF50', '#03A9F4', '#3F51B5', '#00BCD4', '#673AB7', '#f44336', '#00BCD4'],
            fill: '#4CAF50'
        };
    }

    componentDidMount() {
        var self = this;
        setInterval(function() {
            var i = Math.floor(Math.random() * (6 - 0 + 1)) + 0;
            self.setState({fill: self.state.fills[i]});
            console.log(self.refs)
            self.refs.MorphIconRef.morph(self.state.iconScope[i]);
        }, 1600);
    }

    render () {
        return (
          <MorphIcon
            icons={this.state.iconScope} // Icons in the field transformation
            style={{ fill: this.state.fill }} // Styles prop for icon (svg)
            options={{easing: 'quart-in-out', duration: 350}} // options
            size={50} // Icon Size (px)
            ref="MorphIconRef"/>
        );
    }
}
