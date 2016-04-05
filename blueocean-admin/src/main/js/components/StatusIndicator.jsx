import React, { Component, PropTypes } from 'react';
import SvgSpinner from './svgSpinner.jsx';

export default class StatusIndicator extends Component {

    render() {
        const { result } = this.props;
        // early out
        if (!result && !result.toLowerCase) {
            return null;
        }
        const resultClean = result.toLowerCase();
        return (<SvgSpinner
          result={resultClean}
          title={resultClean}
        />);
    }
}
/*
        const classNames = results[resultClean];

 const style = {
 display: 'inline-block',
 width: '32px',
 height: '32px',
 background: results[resultClean],
 borderRadius: '50%',
 };

 <div
 title={resultClean}
 style={style}
 className={classNames}
 >{result}x{classNames}<SvgSpinner
 result={resultClean}
 title={resultClean}/></div>
 */
StatusIndicator.propTypes = {
    result: PropTypes.string.isRequired,
};
