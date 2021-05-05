# Blue Ocean 
Blue Ocean is the next generation user experience for Jenkins. You can learn more about its features on the [Blue Ocean project website](https://jenkins.io/projects/blueocean/).

Join the community and [![Gitter](https://badges.gitter.im/jenkinsci/blueocean-plugin.svg)](https://gitter.im/jenkinsci/blueocean-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

We would also like to thank [![Rollbar](https://d26gfdfi90p7cf.cloudfront.net/rollbar-badge.144534.o.png)](http://rollbar.com) for providing us error reporting.

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

At a minimum you will need a JVM, Maven and python installed, if you are doing active JavaScript development, 
you may want to install NodeJS, but it is not a requirement as the `frontend-maven-plugin` will install
the correct version of Node locally for each plugin to build and develop with.

## Build everything (from root directory)
Builds all maven modules (run this the first time you check things out, at least)

```
$ mvn clean install
```

**NOTE:** If you are using macOS, you must install JDK 1.8 or the installation will fail (The most recent versions of macOS come preinstalled with JDK 10). Please follow [this link](https://stackoverflow.com/questions/46513639/how-to-downgrade-java-from-9-to-8-on-a-macos-eclipse-is-not-running-with-java-9/48422257#48422257) for instructions.

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

NOTE: while running in this mode, Jenkins will automatically re-compile your JavaScript files
and LESS files for all local plugins (including those linked with `hpi:hpl`) where a `package.json` is found
that contains a `mvnbuild` script. If you would like to disable this behavior, you may set
the system property: `-Dblueocean.features.BUNDLE_WATCH_SKIP=true`

## Browser compatibility

The obvious goal is for Blue Ocean to be runnable on all browsers on all platforms. We're not there yet, but getting
closer. The ultimate goal will be to have browser support in line with the [Jenkins Browser Compatibility Matrix](https://wiki.jenkins-ci.org/display/JENKINS/Browser+Compatibility+Matrix). 

List of browsers where we know Blue Ocean is not yet runnable:

* Internet Explorer < 11 on Windows (the aim is to keep IE 11 working, but help is needed to maintain a Windows test environment in the pipeline)

* AmigaOS

## Developing 

Follow the steps above for getting it running first. 

Look in following README's for:
* ``blueocean-rest`` for how to navigate the rest api. 
* ``blueocean-dashboard`` guide on how to modify the GUI in the dashboard plugin. 
* ``blueocean-rest-impl`` for more details on how to actively develop this plugin for backend codebases.


### Building plugins for Blue Ocean

Blue Ocean plugins use the same plugin mechanism as Jenkins for distribution and installation, but involve a lot more JavaScript if they have GUI elements. 

The best way to get started is to look at the tutorial and Yeoman starter project here: 
https://www.npmjs.com/package/generator-blueocean-usain
The usual plugin guide also applies for Jenkins: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial#Plugintutorial-CreatingaNewPlugin 

Ask for help in the gitter room or on the jenkins-ux google group if you are working on a plugin. 

#### Extension points

Blue Ocean has javascript extension points that currently work with react components. This area is a work in progress, and there is no programmatic listing of all the current extension points in a plugin. However, you can grep/search for '<Extensions.Renderer extensionPoint=' and find named extension points. Plugins can contribute to these by declaring what components to plugin in those extension points in their jenkins-js-extension.yaml file (see [link](./blueocean-pipeline-editor/src/main/js/jenkins-js-extension.yaml) for a real world example - the editor implements a few extension points as react components, even css). The editor plugin is a good reference point for blue ocean plugins: [blueocean-pipeline-editor](./blueocean-pipeline-editor).


#### Tools needed

*Maven* is used for most building - install Maven and JDK8 (ideally).

As npm packages are used node-gyp may be involved and this can require some tools installed to build native dependencies (native components are not used at runtime) - see https://www.npmjs.com/package/node-gyp for instructions for your platform

If you are working on the JavaScript, you will need node installed, look at the version in the pom.xml for the minimum version required.

__NOTE__: look in the README.md of the respective modules for more detailed dev docs. 

#### NPM and shrinkwrap

- NOTE: after running `npm install` you will have some copies of Node you can use without installing it globally on your system,
e.g. from the repository root: `PATH=blueocean-web/node:$PATH npm <do-stuff>`
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

#### Source code formatting

We are using [prettier.js](https://prettier.io/) to format JavaScript in order to keep source consistent automatically
rather than with build-time errors about unformatted code. We do this via a pre-commit hook, which you will have to 
enable in your local checkout.

* From the root directory of your `blueocean/` clone, first create the symlink:

````
jdoe@localhost> ln -s ../../bin/pre-commit.js .git/hooks/pre-commit
````
        
* Check the symlink, because if it's wrong git will silently ignore it:

````
jdoe@localhost> file .git/hooks/pre-commit
# => .git/hooks/pre-commit: a /usr/bin/env node script text executable, ASCII text
````

* Check (with no staged changes) to make sure it's going to run successfully in your environment:

````
jdoe@localhost> .git/hooks/pre-commit
# => No staged files to format.
````

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

Watch @jenkinsci on Twitter for frequent updates and news. 

## Upgrading dependencies

If you wish to upgrade dependencies or test an upgrade to something like pipeline (as an example), `pom.xml` in the root of the project should have all the versions specified, a pull request to validate the changes is appreciated. 

If you wanted to see if a new version of a library works with blue ocean: 

* If it isn't published yet, release a beta to the experimental update center
* Open a pull request with the changes to the `pom.xml` in the root of this project (beta dependencies are fine)
* Mark the pull request as "needs-review"
* Make sure to "@mention" people - @michaelneale @vivek are some good ones to start with in a pull request description
* IF the dependency being upgraded is only released to the experimental update center (ie a beta) please also mark the PR as 'DO NOT MERGE' (once it has been released to the main update center, this can be removed)
* Check back later for build success (ie unit tests)
* The Acceptance Test Harness will normally be automatically triggered after a successful PR build, however, it ie best to check it has run: (https://ci.blueocean.io/job/ATH-Jenkinsfile/job/master/) - consult a blue ocean contributor (see below) and they will ensure it has run. This is required for a dependency change.
* Contact a contributor (see below) to let them know of your proposed change so they can review it and do extra testing
* Ensure any dependencies are released to the non beta UC, before merging to master when approved. 

Once the PR is accepted, it will be in use on "dogfood" on ci.blueocean.io/blue - and thus it will be in day to day use almost immediately. If it does bad things, expect to hear about it. 


Contacting contributors: 

Gitter is the day to day chat venue used, you can log in with your github identity.

* look for @michaelneale, @kzantow, @vivek on gitter https://gitter.im/jenkinsci/blueocean-plugin or #jenkins-ux on freenode
* Post to the mailing list: https://groups.google.com/forum/#!forum/jenkinsci-ux


The Acceptance test suite is located here: [acceptance-tests](./acceptance-tests)


## Releasing

When the ATH passes and there is a consensus that a release can be performed: 

* Ensure that the person doing the release has permissions for all the blueocean modules here: https://github.com/jenkins-infra/repository-permissions-updater/tree/master/permissions (or it will fail)
* Switch to the branch to release from (usually master)
* Run a `mvn clean -DcleanNode install -DskipTests` once to clear the decks if you are working on other branches
* Perform the release: 

```
$ mvn release:prepare -DautoVersionSubmodules=true
$ mvn release:perform
```

This will take a while to build and upload. 

* Update release notes on the wiki page

It will take a few hours to propagate to UC. 
