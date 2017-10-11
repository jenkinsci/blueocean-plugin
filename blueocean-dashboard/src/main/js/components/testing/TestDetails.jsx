import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';

/* eslint-disable max-len */

const ConsoleLog = ({ text, className, key = 'console' }) => (
    <div className={`${className} console-log insert-line-numbers`}>
        {text
            .trim()
            .split('\n')
            .map((line, idx) => (
                <div className="line" id={`#${key}-L${idx}`} key={`#${key}-L${idx}`}>
                    {line}
                </div>
            ))}
    </div>
);

ConsoleLog.propTypes = {
    text: PropTypes.string,
    className: PropTypes.string,
    key: PropTypes.string,
};

@observer
class TestDetails extends Component {
    propTypes = {
        duration: PropTypes.number,
        test: PropTypes.array,
        stdout: PropTypes.string,
        stderr: PropTypes.string,
        translation: PropTypes.object,
    };

    render() {
        const test = this.props.test;
        const duration = this.props.duration;
        const stdout = this.props.stdout;
        const stderr = this.props.stderr;
        const translation = this.props.translation;
        return (
            <div>
                <div className="test-details">
                    <div className="test-detail-text" style={{ display: 'none' }}>
                        {duration}
                    </div>
                </div>
                <div className="test-console">
                    {test.errorDetails && <h4>{translation('rundetail.tests.results.error.message', { defaultValue: 'Error' })}</h4>}
                    {test.errorDetails && <ConsoleLog className="error-message" text={test.errorDetails} key={`${test}-message`} />}
                    {test.errorStackTrace && <h4>{translation('rundetail.tests.results.error.output', { defaultValue: 'Stacktrace' })}</h4>}
                    {test.errorStackTrace && <ConsoleLog className="stack-trace" text={test.errorStackTrace} key={`${test}-stack-trace`} />}
                    {stdout && <h4>{translation('rundetail.tests.results.error.stdout', { defaultValue: 'Standard Output' })} </h4>}
                    {stdout && <ConsoleLog className="stack-trace" text={stdout} key={`${test}-stdout`} />}
                    {stderr && <h4>{translation('rundetail.tests.results.error.stderr', { defaultValue: 'Standard Error' })} </h4>}
                    {stderr && <ConsoleLog className="stack-trace" text={stderr} key={`${test}-stderr`} />}
                </div>
            </div>
        );
    }
}

export default TestDetails;
