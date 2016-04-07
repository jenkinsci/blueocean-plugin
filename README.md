This is BlueOcean repo. It is a multi-module maven project. Each sub-directory at the root of the repo is jenkins extension. 



Blue Ocean is the new UI project for Jenkins.

![Pirate logo, because it's ocean and stuff](logo-yarrr.png)
Yarr...

# Modules of note
(check the readme in each of them!)

## blueocean-admin

Example plugin - a starting point. Take a look at it if you want to extend blue ocean. 

## blueocean-web

BlueOcean Web module, core web module with layout and extention points. You probably want to look at the README for this for how to play with it. This should mostly be infrastructure and core components.

## blueocean-plugin

Wraps up blueocean into a Jenkins plugin to run with an embedded Jenkins, provides implementation of REST interfaces needed for embedded mode. 

## blueocean-commons

Common libraries for various modules

## blueocean-rest

Utilities and interfaces for the HTTP api that the Blue Ocean front end needs. 

    
# Building

## Build everything (from root directory)
Builds all maven modules (run this the first time you check things out, at least)

```
$ mvn clean install
```

# Running Blue Ocean

Go into blueocean-web and follow the README.md!


# Debug and live reload with IntelliJ
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
