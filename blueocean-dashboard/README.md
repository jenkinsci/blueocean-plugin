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


## Development hints

### redux

To follow the explanations I recommend you are familiar with http://redux.js.org//docs/basics/index.html.

The basic idea behind our implementation is based on the three principles of redux http://redux.js.org/docs/introduction/ThreePrinciples.html: 

#### 1. Single source of truth - The state of your whole application is stored in an object tree within a single store.

I implemented our single source of truth in `blueocean-web/src/main/js/main.jsx` where we get all `jenkins.main.stores` extensions. If we have plugins that are implementing the redux store then we use this information (basically we chain the exposed reducer - see 3.) to configure the store. 

```javascript
    const stores = ExtensionPoint.getExtensions("jenkins.main.stores");
    let store;
    if (stores.length === 0) {
        store = configureStore(()=>null); // No store, dummy functions
    } else {
        store = configureStore(combineReducers(Object.assign(...stores)));
    }

    // Start React
    render(
        <Provider store={store}>
            <Router history={history}>{ makeRoutes() }</Router>
        </Provider>
      , rootElement);
```

#### 2. State is read-only - The only way to mutate the state is to emit an action, an object describing what happened.

In `blueocean-dashboard/src/main/js/redux/actions.js` we have defined all admin related actions. We call some of this actions  e.g. `fetchRunsIfNeeded` from the view e.g. `Activity.jsx`

```javascript
    componentWillMount() {
        if (this.context.config && this.context.params) {
            const {
                params: {
                    pipeline,
                },
                config = {},
            } = this.context;
            config.pipeline = pipeline;
            this.props.fetchRunsIfNeeded(config);
        }
    }

```

#### 3. Changes are made with pure functions - To specify how the state tree is transformed by actions, you write pure reducers.

> Reducers are just pure functions that take the previous state and an action, and return the next state. Remember to return new state objects, instead of mutating the previous state.

`blueocean-dashboard/src/main/js/redux/reducer.js` here we define all the reducer we are currently using in the admin app and expose them. To follow along the above code snippet from `Activity.jsx` the `fetchRunsIfNeeded` looks like:

```javascript
    fetchRunsIfNeeded(config) {
        return (dispatch) => {
            const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
            `/pipelines/${config.pipeline}/runs`;
            return dispatch(actions.fetchIfNeeded({
                url: baseUrl,
                id: config.pipeline,
                type: 'runs',
            }, {
                current: ACTION_TYPES.SET_CURRENT_RUN_DATA,
                general: ACTION_TYPES.SET_RUNS_DATA,
                clear: ACTION_TYPES.CLEAR_CURRENT_RUN_DATA,
            }));
        };
    },

    fetchIfNeeded(general, types) {
        return (dispatch, getState) => {
            const data = getState().adminStore[general.type];
            dispatch({ type: types.clear });

            const id = general.id;

            if (!data || !data[id]) {
                return fetch(general.url)
                    .then(response => response.json())
                    .then(json => {
                        dispatch({
                            id,
                            payload: json,
                            type: types.current,
                        });
                        return dispatch({
                            id,
                            payload: json,
                            type: types.general,
                        });
                    })
                    .catch(() => dispatch({
                        id,
                        payload: [],
                        type: types.current,
                    })
                    );
            } else if (data && data[id]) {
                dispatch({
                    id,
                    payload: data[id],
                    type: types.current,
                });
            }
            return null;
        };
    },
```

If you have followed a bit the admin app this code reminds a lot of the old "fetch" aka AjaxHoc aka ghettoAjax code. The basic idea is to see if we already have the data in our state `const data = getState().adminStore[general.type];` and if so dispatch `ACTION_TYPES.SET_CURRENT_RUN_DATA` with `payload: data[id],` if not we go ahead and `fetch(general.url)` and using promises to finally dispatch first `ACTION_TYPES.SET_CURRENT_RUN_DATA` and then `ACTION_TYPES.SET_RUNS_DATA`

```javascript
    [ACTION_TYPES.SET_CURRENT_RUN_DATA](state, { payload }): State {
        return state.set('currentRuns', payload);
    },
    [ACTION_TYPES.SET_RUNS_DATA](state, { payload, id }): State {
        const runs = state.get('runs') || {};
        runs[id] = payload;
        return state.set('runs', runs);
    },

```

Now coming back to reducers you see in the end we return a new State were we set 'currentRuns' and 'runs'. Since actions are not synchronous we are exposing the runs/currentRuns in the reducer as 

```javascript
export const runs = createSelector([adminStore], store => store.runs);
```

We use https://github.com/reactjs/reselect here to compute derived data, allowing Redux to store the minimal possible state. For example to see whether the current pipeline is a multibranch pipe:

```javascript
export const isMultiBranch = createSelector(
    [pipeline], (pipe) => {
        if (pipe && pipe.organization) {
            return !!pipe.branchNames;
        }
        return null;
    }
);
```

In e.g. `RunDetails.jsx` we are using both selector like:

```javascript
import {
    actions,
    currentRuns as runsSelector,
    isMultiBranch as isMultiBranchSelector,
    createSelector,
    connect,
} from '../redux';
//...
const selectors = createSelector(
    [runsSelector, isMultiBranchSelector],
    (runs, isMultiBranch) => ({ runs, isMultiBranch }));

export default connect(selectors, actions)(RunDetails);
```

`connect` injects the selectors and actions to our class via properties so we can use them in the `render()`:

```javascript
// e.g. if isMultiBranch is null this means that the "current" pipeline is not set
// early out
        if (!this.context.params
            || !this.props.runs
            || this.props.isMultiBranch === null
        ) {
            return null;
        }

//...using the runs selector:
const result = this.props.runs.filter(...);
 

```
