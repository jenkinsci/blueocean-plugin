This is BlueOcean repo. It is a multi-module maven project. Derived from Castle code base. Each sub-directory at the root of the repo is jenkins extension.

Blue Ocean is the new UI project for Jenkins.

*Please do not commit anything in here that you do not intend to make open source*

![Pirate logo, because it's ocean and stuff](logo-yarrr.png)


# Module Breakdown

## blueocean-web
BlueOcean Web module, everything web related lives here including root url handling. 

* js
    * blueocean.js
        * It is the entry point for blueocean UI. It defines extension point and for demonstration it defines dependency on another  extension **name**

* java
    * blueocean
        * BlueOceanUI.java is entry point for blue ocean backend

* resources
    * *io/blueocean/BlueOceanUI/index.jelly* is equivalent of index.html. We needed this jelly file to inject certain run time variables.

* pom.xml
    * *metrics* and *variant* plugins are there for example to show this is where we can add dependency on external plugins

This is where you run the app to include all plugins along with core. During development it can be used to include all extensions.

## blueocean-plugin

BlueOcean Jenkins plugin. 

# Build and Run

    mvn clean install
    mvn clean install -DskipTests # to skip running tests

## Build and run BlueOcean Jenkins plugin
```
$ cd blueocean-plugin
$ mvn hpi:run
```

- Open http://localhost:8080/jenkins/bo in your browser. Also open JS console on the browser to see message printed from blueocean.js.
- Open http://localhost:8080/jenkins/hello in your browser. "Hello World" is printed by BlueOceanUI.doHello() method.

## To do javascript development

If you wish to make changes to blueocean.js, then you will need to install gulp (http://gulpjs.com/), and then either run:

>TODO: maven profile that defines dependency on blueocean-plugin to do UI development

```
$ ./dev_core.sh
```

or the following:

```
$ gulp rebundle
```
(or run gulp, after each change) in the core directory. This will pick up source changes to commonjs modules (and other things) and put them in target for you (running gulp will run js unit tests too).


## Build and run all module
Builds all modules (except war), basically includes all extensions to let you test everything together during development. Also produces blueocean-all.hpi that can possibly be installed as plugin on Jenkins.

```
$ cd all
$ mvn hpi:run
```

## Build everything (from root directory)
Builds all maven modules.

```
$ mvn clean install
```

# Running Blue Ocean

## Setup

You will need to setup a `app.properties` file before you can run blue ocean.

From the root:
```
$ cp app.properties.example app.properties
```

Then create a new [Github application](https://github.com/settings/developers) and add the client id and secret to the `app.properties` file. Without this you will not be able to sign-in to Blue Ocean.

The `Authorization callback URL` must be set to `http://localhost:8080/loginAction/authenticate/github`

## Run

```
$ ./bin/start.sh
```

# Debug and live reload with IntelliJ (Recommended)
Automatically deploys changes to an instance of blueocean that is run with hpi:run.

1. Enable class reloading: Preferences > Build, Execution, Deployment > Debugger > HowSwap
  * Reload classes in background
  * Reload classes after compilation: always
2. Create a Maven Run/Debug configuration
 * Working Directory: `<project root>/all`
 * Command `hpi:run`
 * Runner > Properties: Enable Skip tests
 * Runner > VM Options: `-Dblueocean.config.file=../app.properties`
3. Debug new configuration, and after compilation the class file will be reloaded

# Debug with remote debugger

To have the Java debugger listen on port 8000:

```
$ ./bin/start.sh --debug
```
