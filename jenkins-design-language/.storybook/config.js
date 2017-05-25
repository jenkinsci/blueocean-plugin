import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/js/stories/index.jsx');
}

configure(loadStories, module);
