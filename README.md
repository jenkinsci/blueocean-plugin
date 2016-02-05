This is BlueOcean repo. It is a multi-module maven project. Derived from Castle code base. Each sub-directory at the root of the repo is jenkins extension. 

Blue Ocean is the new UI project for Jenkins. 

*Please do not commit anything in here that you do not intend to make open source*

![Pirate logo, because it's ocean and stuff](logo-yarrr.png)


# Module Breakdown

## core
All core functionality code lives here.
    
* js
    * blueocean.js 
        * It is the entry point for blueocean UI. It defines extension point and for demonstration it defines dependency on another  extension **name**

* java
    * embryo
        * Replaces classic jenkins, will eventually be replaced by embryo code
    * blueocean
        * BlueOceanUI.java is entry point for blue ocean backend

* resources
    * *io/blueocean/BlueOceanUI/index.jelly* is equivalent of index.html. We needed this jelly file to inject certain run time variables.

* pom.xml
    * *metrics* and *variant* plugins are there for example to show this is where we can add dependency on external plugins

## alpha and bravo 

These are just example of extensions. In due course we will be adding such extensions to this repo. 

## all 

This is where you run the app to include all plugins along with core. During development it can be used to include all extensions.

## war

Create war file from all modulle to be run on CLI as *java -jar blueocean.war*

# Build and Run

## Build and run core module
```
$ cd core
$ mvn hpi:run
```

- Open http://localhost:8080/jenkins in your browser. Also open JS console on the browser to see message printed from blueocean.js.
- Open http://localhost:8080/jenkins/hello in your browser. "Hello World" is printed by BlueOceanUI.doHello() method.

## To do javascript development

If you wish to make changes to blueocean.js, then you will need to install gulp (http://gulpjs.com/), and then either run: 

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

## Hotswap reloading of .class files with IntelliJ
Automatically deploys changes to an instance of blueocean that is run with hpi:run.

1. Enable class reloading: Preferences > Build, Execution, Deployment > Debugger > HowSwap
  * Reload classes in background
  * Reload classes after compilation: always
2. Create a Maven Run/Debug configuration
 * Working Directory: `<project root>/all`
 * Command `hpi:run`
 * Runner > Properties: Enable Skip tests
3. Debug new configuration, and after compilation the class file will be reloaded