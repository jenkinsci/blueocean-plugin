import React, { Component, PropTypes } from 'react';
import SvgDuration from './SvgDuration.jsx';
import SvgTime from './SvgTime.jsx';
import SvgError from './SvgError.jsx'
import SvgSuccess from './SvgSuccess.jsx';

const { object, string } = PropTypes;

class PipelineResult extends Component {
    render() {
        const {
            result = 'failure',
            colors = {
                backgrounds: {
                    success: '#5ba504', // @panel-success-heading-bg
                    failure: '#C4000A', // @panel-danger-heading-bg
                },
            },
        } = this.props;

        return (<div
          style={{
              width: '100%',
              margin: '20px auto',
              backgroundColor: colors.backgrounds[result], // @panel-success-heading-bg
              fontFamily: 'Lato-Semibold, Lato', // @font-family-panel
              color: '#ffffff',
          }}
        >
            <section
              style={{
                  float: 'left',
              }}
            >
                { result === 'success' && <SvgSuccess />}
                { result === 'failure' && <SvgError />}
            </section>
            <section
              style={{
                  float: 'right',
              }}
            >
                <div>
                    <SvgDuration />
                    3 minutes and 42 seconds
                </div>
                <div>
                    <SvgTime />
                    14 minutes ago
                </div>
            </section>
            <section
              style={{
                  width: '60%',
              }}
            >
                <div>CloudBees / Panther #</div>
                <div>Branch master</div>
                <div>Commit #601366d</div>
                <div>Changes by Michael Neale, Ben Waldo and Ivan Meredith</div>
            </section>
            <nav className="page-tabs">
                <a className="selected">Pipeline</a>
                <a>Changes</a>
                <a>Tests</a>
                <a>Artifacts</a>
            </nav>
        </div>);
    }
}

PipelineResult.propTypes = {
    result: string.isRequired,
    colors: object,
};

export {
    PipelineResult,
    SvgDuration,
    SvgTime,
    SvgError,
    SvgSuccess,
};
