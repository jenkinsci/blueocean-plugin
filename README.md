Blue Ocean is the next generation user experience for Jenkins.

It is a multi-module maven project with a few Jenkins plugins. 

Read it: 
https://jenkins.io//blog/2016/05/26/introducing-blue-ocean/




![Pirate logo, because it's ocean and stuff](logo-yarrr.png)
Yarr...

# Modules of note
## blueocean-dashboard

Blue Ocean Dashboard plugin. Currently contains the bulk of the Blue Ocean user interface. This is mostly client side JavaScript built with ES6 and React. 

## blueocean-plugin

Acts as an aggregator plugin, making it an easy place from which to run Blue Ocean via `hpi:run`. 

__NOTE__: As already stated, this plugin is likely to be refactored in the near future.


## blueocean-rest

Java interfaces and classes that specify the definition of the REST API. See the README within this module for more information.

## blueocean-rest-impl

Provides the default implementation of the REST Api defined in the `blueocean-rest` plugin.

## blueocean-web

Web infrastructure that glues Jenkins and Blue Ocean plugin together on the /blue endpoint. 

    
# Building and running

## Build everything (from root directory)
Builds all maven modules (run this the first time you check things out, at least)

```
$ mvn clean install
```

## Running Blue Ocean

```
$ cd blueocean-plugin
$ mvn hpi:run
```

Then open http://localhost:8080/jenkins/blue to start using Blue Ocean.

The Jenkins Classic UI exists side-by-side at its usual place at http://localhost:8080/jenkins.

# Developing 

Follow the steps above for getting it running first. 

Look in following README's for:
* ``blueocean-dashboard`` guide on how to modify the GUI in the dashboard plugin. https://github.com/cloudbees/blueocean-sample-pipeline-result-ext-plugin has a video/sample of a plugin that extends Blue Ocean. 
* ``blueocean-rest`` for how to navigate the rest api. 
* ``blueocean-rest-impl`` for more details on how to actively develop this plugin for backend codebases.

### Tools needed

*Maven* is used for most building. The project is configured to grab all the tools you need from the JavaScript ecosystem to get started. 

If you are working on the Javascript, you will need node and gulp installed.


__NOTE__: look in the README.md of the respective modules for more detailed dev docs. 



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

# Help

Need help? 

You can chat to folks on #jenkins-ux on freenode (IRC). You can also email the jenkins-dev email list (google group: https://groups.google.com/forum/#!forum/jenkinsci-dev) - but ensure you use the prefix [Blue Ocean] in your subject line when posting.

# Presentations

Advanced front end development with react, redux and stuff by @scherler: https://docs.google.com/presentation/d/1dbaYTIGjGT9xX1JnWnaqjMumq94M9nGwljfMQaVtUFc/edit?usp=sharing
