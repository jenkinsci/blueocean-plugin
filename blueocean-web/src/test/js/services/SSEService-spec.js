import { assert } from 'chai';

import { SSEService } from '../../../src/js/services/SSEService';

describe('SSEService', () => {
    let sseService;

    beforeEach(() => {
        sseService = new SSEService();
    });

    it('add a handler', () => {
        const id = sseService.registerHandler(() => {});

        assert.equal(sseService._handlers.length, 1);
        assert.equal(sseService._handlers[0].id, id);
    });

    it('removes a handler', () => {
        const id1 = sseService.registerHandler(() => {});
        const id2 = sseService.registerHandler(() => {});
        const id3 = sseService.registerHandler(() => {});
        const otherIds = [id1, id3];

        sseService.removeHandler(id2);

        assert.equal(sseService._handlers.length, 2);
        sseService._handlers.forEach(handler => {
            assert.notEqual(handler.id, id2);
            assert.isTrue(otherIds.indexOf(handler.id) !== -1);
        });
    });
});
