import { assert } from 'chai';

import { Enum } from '../../../../main/js/creation/flow2/Enum';

describe('Enum', () => {
    describe('construction', () => {
        it('should populate the keys and values', () => {
            const Suit = new Enum({
                SPADES: 'spades',
                CLUBS: 'clubs',
                DIAMONDS: 'diamonds',
                HEARTS: 'hearts',
            });

            assert.equal(Suit.SPADES, 'SPADES');
            assert.equal(Suit.CLUBS, 'CLUBS');
            assert.equal(Suit.DIAMONDS, 'DIAMONDS');
            assert.equal(Suit.HEARTS, 'HEARTS');
        });
    });
    describe('values', () => {
        it('should return an erray with all values', () => {
            const Suit = new Enum({
                SPADES: 'spades',
                CLUBS: 'clubs',
                DIAMONDS: 'diamonds',
                HEARTS: 'hearts',
            });

            assert.equal(Suit.values().length, 4);
            assert.equal(Suit.values()[0], 'SPADES');
            assert.equal(Suit.values()[1], 'CLUBS');
            assert.equal(Suit.values()[2], 'DIAMONDS');
            assert.equal(Suit.values()[3], 'HEARTS');
        });
    });
});
