import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/main/js/stories/index');
}

configure(loadStories, module);
