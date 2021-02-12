# Web middleware

This module mostly contains middleware for serving up "/blue" GUI.
In general, you shouldn't need to modify stuff in for plugins or features as it is infrastructure. Move along.

Look for blueocean.js for excitement.

## how this works with JavaScript

All runtime JavaScript artifacts are generated during the build and added to the plugin HPI.

If you wish to make JavaScript changes during development and have them picked while using the HPI plugin (`mvn hpi:run`),
then you will need to run `npm run bundle` after each change. Alternative, you can run `npm run bundle:watch` to "watch"
for changes and automatically run `npm run bundle`. This applies to all Blue Ocean modules/plugins.
