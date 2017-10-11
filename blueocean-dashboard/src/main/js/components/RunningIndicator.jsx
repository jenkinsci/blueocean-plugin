import React, { Component, PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';

const { number } = PropTypes;

/*
DEMO of running state with raising percentages
 */
export default class RunningIndicator extends Component {
    constructor(props) {
        super(props);
        const initialPercentage = props.percentage || 12.5;
        this.state = { percentage: initialPercentage };
        this.tick = () => {
            // FIXME: remove this.tick code when ux-206 is fixed
            if (this.state.percentage === 100 - initialPercentage) {
                this.setState({ percentage: 100 });
                clearInterval(this.timer);
            } else {
                this.setState({ percentage: this.state.percentage + initialPercentage });
            }
        };
    }
    /* FIXME:
     remove all interval related code when ux-206 is fixed
     start
     */
    componentDidMount() {
        this.timer = setInterval(this.tick, 500);
    }
    componentWillUnmount() {
        clearInterval(this.timer);
    }
    /* FIXME:
     remove all interval related code when ux-206 is fixed
     stop
     */
    render() {
        const props = {
            result: 'running',
            title: 'running',
            percentage: this.state.percentage,
        };
        return <StatusIndicator {...Object.assign({}, this.props, props)} />;
    }
}

RunningIndicator.propTypes = {
    percentage: number,
};
