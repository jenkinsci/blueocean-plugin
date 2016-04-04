import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/main/js/components/stories/pipelines');
}

configure(loadStories, module);
