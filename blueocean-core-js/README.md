# Blue Ocean Core JS

This is an npm module that contains common javascript libraries/utilities that are used across modules and plugins in Blue Ocean.
 
# For Developers

The blueocean-core-js is **not** automatically incorporated into full builds of the blueocean-plugin project.
It is published as its own npm module which is then referenced in other Blue Ocean plugins, notably:
- blueocean-web (note that since "web" is responsible for "bootstrapping" the Blue Ocean UI, the version specified in blueocean-web/package.json is the version provided to all other modules)
- blueocean-dashboard
- blueocean-personalization

## Developing with Prerelease Versions

To test changes in blueocean-core-js, perform the following steps:
- Tick the version number in package.json and npm-shrinkwrap.json and ensure a "prelease" version specified, e.g. 0.0.90-SNAPSHOT-1
- `npm run gulp`
- `npm publish --tag beta` (the "beta" tag is important)
   - If you receive an error about the package already existing, tick the version up again, e.g. 0.0.90-SNAPSHOT-2 and repeat above steps.
- Then update the modules that reference core-js:
   - With script: `bin/cleanInstall.js @jenkins-cd/blueocean-core-js@0.0.90-SNAPSHOT-1`
   - Or manually in blueocean-web, blueocean-dashboard, and blueocean-personalization
      - `npm install @jenkins-cd/blueocean-core-js@0.0.90-SNAPSHOT-1 -S -E`
- You should see all package.json and npm-shrinkwrap.json updated to reflect the new version number.
- Push changes to your branch and ensure that the blueocean-plugin and ATH builds both pass.

## Publishing a Production Version

Once the changes to blueocean-core-js are approved via PR, perform the following steps:
- Ensure your branch is 100% up to date with master. **This is critical**, otherwise recent changes may be lost and break the app.
   - If changes to core-js were made in the interim, ideally you should merge and publish a new pre-release version to ensure builds still pass.
- Tick version numbers in package.json and npm-shrinkwrap.json to a production version, e.g. "0.0.90"
- `npm run gulp`
- `npm publish`
- Commit the changes to package.json and npm-shrinkwrap.json
- Tick the version number in package.json and npm-shrinkwrap.json and ensure a "prelease" suffix specified, e.g. 0.0.91-SNAPSHOT
- Commit the changes to package.json and npm-shrinkwrap.json
- Then update the modules that reference core-js:
   - With script: `bin/cleanInstall.js @jenkins-cd/blueocean-core-js@0.0.90`
   - Or manually in blueocean-web, blueocean-dashboard, and blueocean-personalization
      - `npm install @jenkins-cd/blueocean-core-js@0.0.90 -S -E`
- You should see all package.json and npm-shrinkwrap.json updated to reflect the new version number.
- Push changes to your branch and ensure that the blueocean-plugin and ATH builds both pass.
- Merge your PR to master.

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
