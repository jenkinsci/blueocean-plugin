This is BlueOcean repo. It is a multi-module maven project. Derived from Castle code base. Each sub-directory at the root of the repo is jenkins extension. 

Blue Ocean is the new UI project for Jenkins. 

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

# Build and Run

## Build and run core module
```
$ cd core
$ mvn hpi:run
```

- Open http://localhost:8080/jenkins in your browser. Also open JS console on the browser to see message printed from blueocean.js.
- Open http://localhost:8080/jenkins/hello in your browser. "Hello World" is printed by BlueOceanUI.doHello() method.

## To do javascript development

If you wish to make changes to blueocean.js, then you will need to run:

```
$ gulp rebundle
```
(or run gulp, after each change) in the core directory. This will pick up source changes to commonjs modules (and other things) and put them in target for you (running gulp will run js unit tests too). 


## Build and run all module (includes all extensions found in this repository)
```
$ cd all
$ mvn hpi:run
```
