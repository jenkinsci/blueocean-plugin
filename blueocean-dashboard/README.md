# Dashboard plugin

This plugin provides the main Dashboard user interface for Blue Ocean.
It has a bunch of GUI components and extension points for other plugins to extend. 
This is where the fun happens. 

## Running and modifying this plugin

### With mvn

1. Go into `blueocean` and run `mvn hpi:run` in a terminal. (mvn clean install from the root of the project is always a good idea regularly!)
2. From this directory, run `npm run bundle:watch` to watch for JS changes and reload them.
3. Open browser to http://localhost:8080/jenkins/blue/ to see this
4. hack away. Refreshing the browser will pick up changes. If you add a new extension point or export a new extension you may need to restart the `mvn hpi:run` process. 

### With npm/storybook

We are supporting React Storybook https://voice.kadira.io/introducing-react-storybook-ec27f28de1e2#.8zsjledjp 

```javascript
npm run storybook
```

Then it’ll start a webserver on port 9001. Further on any change it will
refresh the page for you on the browser. The design is not the same as 
in blueocean yet but you can fully develop the components without a 
running jenkins,

#### Writing Stories

Basically, a story is a single view of a component. It's like a test case, but you can preview it (live) from the Storybook UI.

You can write your stories anywhere you want. But keeping them close to your components is a pretty good idea.

Let's write some stories:

```javascript
// src/main/js/components/stories/button.js

import React from 'react';
import { storiesOf, action } from '@kadira/storybook';

storiesOf('Button', module)
  .add('with a text', () => (
    <button onClick={action('clicked')}>My First Button</button>
  ))
  .add('with no text', () => (
    <button></button>
  ));
```

Here, we simply have two stories for the built-in `button` component. But, you can import any of your components and write stories for them instead.

#### Configurations for storybook

Now you need to tell Storybook where it should load the stories from. For that, you need to add the new story to the configuration file `.storybook/config.js`:


```javascript
// .storybook/config.js
import { configure } from '@kadira/storybook';

function loadStories() {
  require('../src/main/js/components/stories/index');
  require('../components/stories/button'); // add this line
}

configure(loadStories, module);
```

or to the `src/main/js/components/stories/index.js` (this is the preferred way):

```javascript
// src/main/js/components/stories/index.js
require('./pipelines');
require('./status');
require('./button'); // add this line
```

That's it. Now simply run “npm run storybook” and start developing your components.

# Running Tests

Tests are run via jest using the gulp-jest plugin. Three modes of execution are supported:

## `npm run test`

Runs jest and outputs JUnit test reports and code coverage metrics, in the 'reports' and 'coverage' dirs.
This is the "full" execution that is run in CI.

## `npm run test-fast`

Runs jest without test reports or coverage. Fastest run, useful for local development.

## `npm run test-debug`

Runs jest in debug mode listening on localhost:5858. You must attach a debugger for execution to proceed.
Test reports and coverage are skipped. 

## Running select test(s)

All of the above profiles support executing one or more tests via jest's `testPathPattern` parameter:

`npm run test:fast -- --test test/js/UrlUtils-spec.js # one test `

`npm run test:fast -- --test /capability/ # any tests in a 'capability' dir`

`npm run test:fast -- --test Url # any test with 'Url' in the name`


### Linting with npm

ESLint with React linting options have been enabled.

```
npm run lint
```

#### lint:fix

You can use the command lint:fix and it will try to fix all
offenses, however there maybe some more that you need to fix manually.

```
npm run lint:fix
```

#### lint:watch

You can use the command lint:watch and it will give rapid feedback 
(as soon you save, lint will run) while you try to fix all offenses.

```
gulp lint:watch --continueOnLint
```
