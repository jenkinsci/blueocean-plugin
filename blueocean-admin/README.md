# Example plugin

This plugin is an example of a few extensions
The extension points are defined in `blueocean-web`

## Running this

1) Go into `blueocean-plugin` and run `mvn hpi:run` in a terminal. (mvn clean install from the root of the project is always a good idea regularly!)
2) From this directory, run `gulp bundle:watch` to watch for JS changes and reload them.
3) Open browser to http://localhost:8080/jenkins/blue/ to see this
4) hack away. Refreshing the browser will pick up changes. If you add a new extension point or export a new extension you may need to restart the `mvn hpi:run` process. 
