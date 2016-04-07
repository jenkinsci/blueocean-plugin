import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/main/js/components/stories/pipelines');
  require('../src/main/js/components/stories/log');
}

configure(loadStories, module);
