import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/main/js/components/stories/index');
}

configure(loadStories, module);
