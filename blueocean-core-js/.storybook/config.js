import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/js/components/stories/index');
}

configure(loadStories, module);
