import React from 'react';
import { expect } from 'chai';
import { render } from 'enzyme';
import RunMessageCell from '../../main/js/components/RunMessageCell';

describe('RunMessageCell', () => {

    it('displays description', () => {
        const run =  { description: 'The cake is a lie' };
        const cell = render(<RunMessageCell run={run} />);
        expect(cell.text()).to.equal('The cake is a lie');
    });

    it('displays changeSet', () => {
        const run =  {
            'changeSet':[{
                'msg': 'Oops',
            }, {
                'msg': 'fix bug',
            }]
        };
        const cell = render(<RunMessageCell run={run} />);
        expect(cell.text()).to.equal('fix bug');
    });

    it('displays cause', () => {
        const run = {
            causes: [
                { shortDescription: 'Cake is delicious' },
                { shortDescription: 'Have some cake' },
            ]
        };
        const cell = render(<RunMessageCell run={run} />);
        expect(cell.text()).to.equal('Cake is delicious');
    });

    it('displays nothing', () => {
        const cell = render(<RunMessageCell run={null} />);
        expect(cell.text()).to.equal('–');
    });
});
