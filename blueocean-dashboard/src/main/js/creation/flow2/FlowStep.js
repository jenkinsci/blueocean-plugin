import React, { PropTypes } from 'react';
import VerticalStep from './VerticalStep';
import Status from './FlowStepStatus';

const SCROLL_DELAY_MILLIS = 50;


/**
 * Visual/logic component that defines an individual step of a multi-step workflow.
 * Intended to be used within a MultiStepFlow component.
 * Hides all content except for the title until the step becomes active.
 */
export default class FlowStep extends React.Component {

    componentDidMount() {
        this._adjustScrolling({}, this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._adjustScrolling(this.props, nextProps);
    }

    componentWillUnmount() {
        this._cleanup();
    }

    scrollTimeoutId = null;

    _bindStep(step) {
        this.step = step;
    }

    _cleanup() {
        if (this.scrollTimeoutId) {
            clearTimeout(this.scrollTimeoutId);
            this.scrollTimeoutId = 0;
        }
    }

    _adjustScrolling(oldProps, nextProps) {
        if (!nextProps.scrollOnActive) {
            return;
        }

        // only scroll if we've become active for the first time
        if (nextProps.status === Status.ACTIVE && nextProps.status !== oldProps.status) {
            this._cleanup();

            setTimeout(() => {
                if (this.step) {
                    this.step.scrollIntoView();
                }
            }, SCROLL_DELAY_MILLIS);
        }
    }

    render() {
        const { props } = this;
        const percentage = props.loading ? 101 : props.percentage;
        const status = props.error ? Status.ERROR : props.status;

        return (
            <VerticalStep
                className={props.className}
                status={status}
                percentage={percentage}
                isLastStep={props.isLastStep}
            >
                <div ref={step => this._bindStep(step)}>
                    <h1>{props.title}</h1>
                    {
                        props.status !== Status.INCOMPLETE &&
                        <fieldset disabled={props.disabled}>
                            {props.children}
                        </fieldset>
                    }
                </div>
            </VerticalStep>
        );
    }
}

FlowStep.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    title: PropTypes.string,
    status: PropTypes.string,
    percentage: PropTypes.number,
    disabled: PropTypes.bool,
    loading: PropTypes.bool,
    error: PropTypes.bool,
    scrollOnActive: PropTypes.bool,
    isLastStep: PropTypes.bool,
};

FlowStep.defaultProps = {
    className: '',
    scrollOnActive: true,
};
