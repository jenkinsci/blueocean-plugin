import { assert } from 'chai';

import { removeMarkupTags } from '../../../src/js/parameter/paramUtil';

describe('paramUtil', () => {
    it('removeMarkupTags', () => {
        // Tag with content
        assert.equal(removeMarkupTags(
            'A <script type="text/javascript">console.log("blah")</script> script'),
            'A console.log("blah") script');

        // Tag without content
        assert.equal(removeMarkupTags(
            'A <b>bold'),
            'A bold');

        // Stray gt/lt chars. Yeah, that's invalid markup,
        // but still a valid test for that function, whose purpose is just to remove
        // the markup tags i.e. it's not about creating perfectly valid markup.
        assert.equal(removeMarkupTags(
            'A <b>bold > </b><'),
            'A bold > <');
    });
});
