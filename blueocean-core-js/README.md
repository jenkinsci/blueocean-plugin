# Blue Ocean Core JS

This is an npm module that contains common javascript libraries/utilities that are used across modules and plugins and blue oceans. 

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

`npm run test-fast -- --test test/js/UrlUtils-spec.js` // one test
`npm run test-fast -- --test /capability/` // any tests in a 'capability' dir
`npm run test-fast -- --test Url` // any test with 'Url' in the name
