import React from 'react';
import { assert } from 'chai';
import { render } from 'enzyme';
import { LogConsole } from '../../main/js/components/karaoke/components/LogConsole';
import cheerio from 'cheerio';

describe('LogConsole', () => {
    it('can be instantiated', () => {
        const logLines = ['red', 'green', 'blue'];
        const wrapper = render(
            <LogConsole logArray={logLines} />,
        );

        const lines = wrapper.find('.line');

        assert.equal(lines.length, 3, '3 lines');
        assert.equal(lines.text(), 'redgreenblue', 'lines text');
    });

    it('supports ansi', () => {
        const logLines = [
            'plain text',
            'Some text is \x1b[32mGREEN\x1b[0m',
            'moar plain text',
        ];

        const wrapper = render(
            <LogConsole logArray={logLines} />,
        );

        // Can't take the weirdness from enzyme, dump to HTML then a plain cheerio obj
        const $ = cheerio.load(wrapper.html());

        // Find the lines as a list of cheerio objects
        const lines = $('.line').toArray().map(nodeInfo => $(nodeInfo));

        assert.equal(3, lines.length, '3 lines');

        assert.equal(lines[0].text(), 'plain text', 'line 1 text');
        assert.equal(lines[1].text(), 'Some text is GREEN', 'line 2 text');
        assert.equal(lines[2].text(), 'moar plain text', 'line 3 text');

        assert.equal(lines[1].html(), 'Some text is <span class="ansi-fg-2">GREEN</span>', 'line 2 as html');
    });

    it('likifies URLs', () => {
        const logLines = [
            'plain text',
            'Includes http://www.example.org/ prefixed URL',
            'Includes example.org unprefixed URL',
        ];

        const wrapper = render(
            <LogConsole logArray={logLines} />,
        );

        // Can't take the weirdness from enzyme, dump to HTML then a plain cheerio obj
        const $ = cheerio.load(wrapper.html());

        // Find the lines, convert to a list of cheerio objects
        const lines = $('.line').toArray().map(nodeInfo => $(nodeInfo));

        assert.equal(3, lines.length, '3 lines');

        assert.equal(lines[0].text(), 'plain text', 'line 1 text');
        assert.equal(lines[1].text(), 'Includes http://www.example.org/ prefixed URL', 'line 2 text');
        assert.equal(lines[2].text(), 'Includes example.org unprefixed URL', 'line 3 text');

        const anchor = lines[1].find('a');
        assert.equal(anchor.length, 1, 'found 1 anchor in line 2');
        assert.equal(anchor.text(), 'http://www.example.org/', 'line 2 anchor text');
        assert.equal(anchor.attr('href'), 'http://www.example.org/', 'line 2 anchor href');

        assert.equal(lines[2].html(), 'Includes example.org unprefixed URL', 'line 3 as html');
    });
});
