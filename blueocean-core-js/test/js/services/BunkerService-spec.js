import { assert } from 'chai';
import { BunkerService } from '../../../src/js/services/BunkerService';

describe('BunkerService', () => {
    let bunkerService;

    beforeEach(() => {
        bunkerService = new BunkerService();
    });

    it('check key is not null', () => {
        var data = JSON.parse(JSON.stringify(require('../data/BunkerService-data-1.json')));
        assert.isNotNull(bunkerService.bunkerKey(data));
    });

    it('check key is not null', () => {
        var data = JSON.parse(JSON.stringify(require('../data/BunkerService-data-2.json')));
        assert.isNotNull(bunkerService.bunkerKey(data));
    });

});
