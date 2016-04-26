import Immutable from 'immutable';

// FIXME: we need something that gets sub-state definitions from the extensions
export const State = Immutable.Record({
    plugins: Immutable.OrderedMap(),
    isFetching: false,
});
