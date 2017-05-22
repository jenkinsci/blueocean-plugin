import React from 'react';
import { expect } from 'chai';
import { render } from 'enzyme';
import RunMessageCell from '../../main/js/components/RunMessageCell';

const t = (m) => { return m; };

describe('RunMessageCell', () => {

    it('displays description', () => {
        const run =  { description: 'The cake is a lie' };
        const cell = render(<RunMessageCell run={run} t={t} />);
        expect(cell.text()).to.equal('The cake is a lie');
    });

    // TODO: mock i18n
    it('displays with multiple commits', () => {
        const run =  {
            'changeSet':[{
                'msg': 'Oops',
            }, {
                'msg': 'fix bug',
            }]
        };
        const cell = render(<RunMessageCell run={run} t={t} />);
        expect(cell.text()).to.equal('fix buglozenge.commit');
    });

    it('displays with single commit', () => {
        const run =  {
            'changeSet':[{
                'msg': 'Oops',
            }]
        };
        const cell = render(<RunMessageCell run={run} t={t} />);
        expect(cell.text()).to.equal('Oops');
    });

    it('displays cause', () => {
        const run = {
            causes: [
                { shortDescription: 'Cake is delicious' },
                { shortDescription: 'Have some cake' },
            ]
        };
        const cell = render(<RunMessageCell run={run} t={t} />);
        expect(cell.text()).to.equal('Cake is delicious');
    });

    it('displays nothing', () => {
        const cell = render(<RunMessageCell run={null} t={t} />);
        expect(cell.text()).to.equal('–');
    });
});
