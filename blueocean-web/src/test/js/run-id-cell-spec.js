import React from 'react';
import { expect } from 'chai';
import { render } from 'enzyme';
import RunIdCell from '../../main/js/components/RunIdCell';

describe('RunIdCell', () => {

    it('displays name', () => {
        const run = { name: '1.0.1', id: 123 };
        const cell = render(<RunIdCell run={run}/>);
        expect(cell.text()).to.equal('1.0.1');
    });

    it('displays id', () => {
        const run = { id: 123 };
        const cell = render(<RunIdCell run={run}/>);
        expect(cell.text()).to.equal('123');
    });
});
