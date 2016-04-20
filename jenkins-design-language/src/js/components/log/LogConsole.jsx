import React, { Component, PropTypes } from 'react';
import {fetch} from '../fetch';
import MorphIcon from 'react-morph-material-icons-clone';

const {string} = PropTypes;
let rawUrl;

var SampleIcon = React.createClass({
    getInitialState() {
        return {
            iconScope: ['done_all', 'favorite_outline', 'explore', 'translate', 'open_with', 'perm_media', 'new_releases'],
            fills: ['#4CAF50', '#03A9F4', '#3F51B5', '#00BCD4', '#673AB7', '#f44336', '#00BCD4'],
            fill: '#4CAF50'
        }
    },

    componentDidMount() {
        var self = this;
        setInterval(function() {
            var i = Math.floor(Math.random() * (6 - 0 + 1)) + 0;
            self.setState({fill: self.state.fills[i]});
            console.log(self.refs)
            self.refs.MorphIconRef.morph(self.state.iconScope[i]);
        }, 1600);
    },

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
});


class LogConsole extends Component {
    render() {
        console.log(this.refs)
         const {data} = this.props;
         //early out
         if (!data) {
            return null;
         }

         let lines = [];
         if (data && data.split) {
            lines = data.split('\n');
         }

        return (<code
              className="block"
            >
                <div className="log-header">
                    <a
                      className="download-log-button right"
                      title="Display the log in plaintext"
                      target="_blank"
                      href={rawUrl}
                    >
          <SampleIcon />
                        <MorphIcon
                          options={{easing: 'quart-in-out', duration: 350}}
                          size={50} // Icon Size (px)
                          ref="MorphIconRef"
                          icons={['file_download']}
                          style={
                            {fill: '#fff'}
                          }
                        /><span className="icon-download-log" aria-hiden="true"></span>Raw log</a>
                </div>
                {lines.map((line, index) => <p key={index}>
                <a key={index} name={index}>{line}</a>
            </p>)}</code>)
    }
}

LogConsole.propTypes = {
    data: string,
    url: string.isRequired,
};

export default fetch(LogConsole, ({url}, config) => {
    rawUrl = config.getAppURLBase() + url;
    return rawUrl;
}, false) ;
