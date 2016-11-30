# Blue Ocean 
Blue Ocean is the next generation user experience for Jenkins. You can learn more about its features and roadmap on the [Blue Ocean project website](https://jenkins.io/projects/blueocean/).

Join the community and [![Gitter](https://badges.gitter.im/jenkinsci/blueocean-plugin.svg)](https://gitter.im/jenkinsci/blueocean-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

# Get Blue Ocean

Blue Ocean is [available from the Jenkins update center](https://jenkins.io/projects/blueocean/#use-the-beta) for Jenkins 2.7.1 and above. 

# Reporting bugs and feature requests

We use the [Jenkins JIRA](https://issues.jenkins-ci.org/) to log all bugs and feature requests. Create a [new account](https://accounts.jenkins.io/), [browse to JIRA](https://issues.jenkins-ci.org/) and login with your account then create a new issue with the component `blueocean-plugin`.

# For Developers
It is a multi-module maven project made up of a few Jenkins plugins. There is an aggregator plugin in the "blueocean" module.

CONTRIBUTIONS ALWAYS WELCOME NO MATTER HOW BIG OR SMALL.

Some background reading: 
https://jenkins.io/blog/2016/05/26/introducing-blue-ocean/

This is in the main Update Center for Jenkins. Install the plugin called "BlueOcean beta" (and let it install its dependencies). The instructions here are for contributors to Blue Ocean and the morbidly curious. Expect refactoring. 


![Pirate logo, because it's ocean and stuff](docu/pix/logo-yarrr.png)
Yarr...

## Modules of note

A quick tour of some of the modules (not all). Consult README.md in respective modules for more info.

### blueocean-dashboard

Blue Ocean Dashboard plugin. Currently contains a lot of the core of the Blue Ocean user interface and extension points. This is mostly client side JavaScript built with ES6 and React. 

### blueocean

An aggregator plugin, making it an easy place from which to run Blue Ocean via `hpi:run`. 

### blueocean-rest

Java interfaces and classes that specify the definition of the REST API that blueocean uses. See the README.md within this module for detail on this api.

### blueocean-rest-impl

Provides the default implementation of the core REST Apis defined in the `blueocean-rest` plugin. It comes with only freestyle job support.


### blueocean-pipeline-api-impl

Provides implementation of Pipeline apis for Jenkins pipeline and multi-branch job types support

### blueocean-web

Core Web infrastructure that bootstraps BlueOcean UI and integrates REST API core blueocean-rest, and serves up the core javascript libraries. 

    
## Building and running

At a minimum you will need JVM and Maven installed, if you are doing active JavaScript development, installing NodeJS is a good idea too. 

## Build everything (from root directory)
Builds all maven modules (run this the first time you check things out, at least)

```
$ mvn clean install
```

For now, you'll need to skip the tests if __building on Windows__, so be sure to include the `-DskipTests` switch e.g.

```
$ mvn clean install -DskipTests
```

### Running Blue Ocean

```
$ mvn -f blueocean/pom.xml hpi:run
```

Then open http://localhost:8080/jenkins/blue to start using Blue Ocean.

The Jenkins Classic UI exists side-by-side at its usual place at http://localhost:8080/jenkins.

## Browser compatibility

The obviously goal is for Blue Ocean to be runnable on all browsers on all platforms. We're not there yet, but getting
closer. The ultimate goal will be to have browser support in line with the [Jenkins Browser Compatibility Matrix](https://wiki.jenkins-ci.org/display/JENKINS/Browser+Compatibility+Matrix). 

List of browsers where we know Blue Ocean is not yet runnable:

* Internet Explorer < 11 on Windows (the aim is to keep IE 11 working, but help is needed to maintain a Windows test environment in the pipeline)

* AmigaOS

## Developing 

Follow the steps above for getting it running first. 

Look in following README's for:
* ``blueocean-dashboard`` guide on how to modify the GUI in the dashboard plugin. https://github.com/cloudbees/blueocean-sample-pipeline-result-ext-plugin has a video/sample of a plugin that extends Blue Ocean. 
* ``blueocean-rest`` for how to navigate the rest api. 
* ``blueocean-rest-impl`` for more details on how to actively develop this plugin for backend codebases.


### Building plugins for Blue Ocean

Blue Ocean plugins use the same plugin mechanism as Jenkins for distribution and installation, but involve a lot more Javascript if they have GUI elements. 

The best way to get started is to look at the tutorial and Yeoman starter project here: 
https://www.npmjs.com/package/generator-blueocean-usain
The usual plugin guide also applies for Jenkins: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial#Plugintutorial-CreatingaNewPlugin 

Ask for help in #jenkins-ci or on the mailing list if you are working on a plugin. 


#### Tools needed

*Maven* is used for most building. The project is configured to grab all the tools you need from the JavaScript ecosystem to get started. 

If you are working on the Javascript, you will need node installed, look at the version in the pom.xml for the minimum version required.

__NOTE__: look in the README.md of the respective modules for more detailed dev docs. 

#### NPM and shrinkwrap

- Ensure your npm is 3.10.8+ as this release fixes some important bugs with shrinkwrap, notably #11735 in [notes](https://github.com/npm/npm/releases/tag/v3.10.8)
- Don't edit package.json directly; use npm install to ensure that both package.json and npm-shrinkwrap.json are updated.
- To add or update a dependency:
   - `npm install packageName@3.2.1 -S -E`
- To add or update a devDependency:
   - `npm install packageName@3.2.1 -D -E`
- If you are handling a merge conflict in package.json, resolve the conflict in the file as normal. Then use
the appropriate command to update each conflicting dependency to ensure shrinkwrap is updated.
- To remove a dependency:
   - `npm uninstall packageName -S`
- To remove a devDependency:
   - `npm uninstall packageName -D`
- If you ever need to create a shrinkwrap for the first time, use `npm shrinkwrap --dev` to ensure devDependencies are
included in the shrinkwrap.

Full docs on [npm shrinkwrap](https://docs.npmjs.com/cli/shrinkwrap)
Information on [building with shrinkwrap](https://docs.npmjs.com/cli/shrinkwrap#building-shrinkwrapped-packages)

In case you want to update your dependencies with something like ```npm-check-updates``` make sure you follow the simple steps:

```
ncu -a
rm -rf node_modules npm-shrinkwrap.json
npm i
npm shrinkwrap --dev
```


## Contributing - help wanted

### i18n - Sprechen Sie Deutsch?

We have full i18n support in our plugins. Please read the [i18n documentation](./docu/I18N.md) on how you can provide new translations and how to work with i18n.

### contributing guidelines

Want to get involve with blueocean? See our [contributing guidelines](./CONTRIBUTING.md) for more informations.


## Debug and live reload with IntelliJ
Automatically deploys changes to an instance of blueocean that is run with hpi:run.

1. Enable class reloading: Preferences > Build, Execution, Deployment > Debugger > HotSwap
  * Reload classes in background
  * Reload classes after compilation: always
2. Create a Maven Run/Debug configuration
 * Working Directory: `<project root>/all`
 * Command `hpi:run`
 * Runner > Properties: Enable Skip tests
 * Runner > VM Options: `-Dblueocean.config.file=../app.properties`
3. Debug new configuration, and after compilation the class file will be reloaded

## Help

Need help? 

You can chat to folks on #jenkins-ux on freenode (IRC). You can also email the jenkins-dev email list (google group: https://groups.google.com/forum/#!forum/jenkinsci-dev) - but ensure you use the prefix [Blue Ocean] in your subject line when posting.

## Presentations

Advanced front end development with react, redux and stuff by @scherler: https://docs.google.com/presentation/d/1dbaYTIGjGT9xX1JnWnaqjMumq94M9nGwljfMQaVtUFc/edit?usp=sharing

Watch @i386 and @jenkinsci on Twitter for frequent updates and news. 
