import React from 'react';
import { assert, expect } from 'chai';
import { render } from 'enzyme';
import RunDescription from '../../main/js/components/karaoke/components/RunDescription';

const t = (m) => { return m; };

describe('RunDescription', () => {
    it('displays description', () => {
        const run = { description: 'The cake is a lie' };
        const cell = render(<RunDescription run={run} t={t} />);
        expect(cell.text()).to.equal(' rundetail.pipeline.descriptionThe cake is a lie');
    });

    it('displays nothing when description unavailable', () => {
        const run = { description: null };
        const cell = render(<RunDescription run={run} t={t} />);
        expect(cell.text()).to.equal('');
    });

    it('displays nothing when run unavailable', () => {
        const run = { description: null };
        const cell = render(<RunDescription run={null} t={t} />);
        expect(cell.text()).to.equal('');
    });
});
