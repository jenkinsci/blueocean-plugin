import { configure } from '@storybook/react';

// automatically import all files ending in *.stories.js
const req = require.context('../src/js/stories', true, /.*Stories\.jsx$/);
function loadStories() {
    require('../less/theme.less');
    req.keys().forEach(filename => req(filename));
}

configure(loadStories, module);
