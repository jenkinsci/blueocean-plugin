/**
 * Creates a "unique" id generator
 */

export type IdGenerator = {
    next(): number;
};

const idgen: IdGenerator = {
    id: 0,
    next() { return --this.id; }
};

export default idgen;
