// @flow

import React, { Component, PropTypes } from 'react';

export class Progress extends Component {
    render() {
        let percentage = this.props.percentage;
        const groupClasses = ['progress-container'];

        if (typeof percentage !== 'number' || isNaN(percentage) || percentage < 0) {
            groupClasses.push('indeterminate');
            percentage = 12.5;
        } else if (percentage > 100) {
            groupClasses.push('bounce');
            percentage = 10;
        }

        return (
            <div>
                <svg xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none" viewBox="0 0 100 10" className={groupClasses.join(' ')}>
                    <line x1="0" y1="5" x2="100" y2="5" className="progress-bg" />
                    <line x1="0" y1="5" x2={percentage} y2="5" className="progress-bar" />
                </svg>
            </div>
        );
    }
}

Progress.propTypes = {
    percentage: PropTypes.number,
};
