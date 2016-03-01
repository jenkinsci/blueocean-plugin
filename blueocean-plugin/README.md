# Embedded Jenkins Plugin

This plugin allows blueocean to run as a plugin inside an existing Jenkins.

## Usage during development

To use this in development: 

`mvn hpi:run`


Blue Ocean UI: 

    http://localhost:8080/jenkins/blue
    
Embedded Jenkins UI (so you can create some pipelines for fun):

    http://localhost:8080/jenkins
    
    
This links source from the other modules. So if you make a source change to rest, web or other modules,
it will be applied here. If its a UI module, you will need to run `gulp rebundle` in the UI modules directory
to watch for JS/HTML changes and live reload them. 
