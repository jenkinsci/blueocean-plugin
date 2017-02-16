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

            assert.equal(Suit.SPADES, 'spades');
            assert.equal(Suit.CLUBS, 'clubs');
            assert.equal(Suit.DIAMONDS, 'diamonds');
            assert.equal(Suit.HEARTS, 'hearts');
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
            assert.equal(Suit.values()[0], 'spades');
            assert.equal(Suit.values()[1], 'clubs');
            assert.equal(Suit.values()[2], 'diamonds');
            assert.equal(Suit.values()[3], 'hearts');
        });
    });
});
