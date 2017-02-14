/**
 * Pseudo-enum.
 *
 * Usage:
 *
 * const Suit = new Enum({
 *     SPADES: 'spades',
 *     CLUBS: 'clubs',
 *     DIAMONDS: 'diamonds',
 *     HEARTS: 'hearts',
 * });
 */
export class Enum {

    _values = [];

    constructor(props) {
        Object.assign(this, props);

        for (const value in props) {
            if (props.hasOwnProperty(value)) {
                this._values.push(props[value]);
            }
        }
    }

    values() {
        return this._values.slice();
    }

}
