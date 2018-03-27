/**
 * Pseudo-enum.
 *
 * Simplest usage:
 * const Suit = new Enum(['SPADES', 'CLUBS', 'DIAMONDS', 'HEARTS']);
 *
 * Alternate usage:
 * NOTE: the underlying values in the enum are the key names, NOT the values.
 * NOTE: this usage may be desirable as it generally leads to better IDE code hints.
 *
 * const Suit = new Enum({
 *     SPADES: 'SPADES',
 *     CLUBS: 'CLUBS',
 *     DIAMONDS: 'DIAMONDS',
 *     HEARTS: 'HEARTS',
 * });
 *
 */
export class Enum {
    _values = [];

    constructor(props) {
        this._addValues(props);
    }

    extend(extraProps) {
        const values = this._values.slice();
        const cloned = new Enum(values);
        cloned._addValues(extraProps);
        return cloned;
    }

    _addValues(props) {
        // support Arrays or "array-like" objects (e.g. ObservableArray)
        if (typeof props.slice === 'function' && Array.isArray(props.slice())) {
            for (const value of props.slice()) {
                this[value] = value;
                this._values.push(value);
            }
        } else if (typeof props === 'object') {
            for (const keyName in props) {
                if (props.hasOwnProperty(keyName)) {
                    this[keyName] = keyName;
                    this._values.push(keyName);
                }
            }
        }
    }

    values() {
        return this._values.slice();
    }
}
