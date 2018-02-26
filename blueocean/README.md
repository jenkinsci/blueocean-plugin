# BlueOcean Aggregator

Aggregates all BlueOcean modules. 

## To start it all up

To use this in development or to tyre kick: 

`mvn hpi:run`

(make sure you have run `mvn clean install` from the parent first)

Blue Ocean UI: 

    http://localhost:8080/jenkins/blue
    
Embedded Jenkins UI (so you can create some pipelines for fun):

    http://localhost:8080/jenkins
    
    
This links source from the other modules. So if you make a source change to rest, web or other modules,
it will be applied here. If its a UI module, you will need to have gulp installed (http://gulpjs.com/) and run `gulp bundle:watch` in the modules directory that has the JS you want to watch for changes and to have the reloaded live. 

# Running Blue Ocean in development

If you want to add to some extension points or mess with the GUI as it is right now, take a look at the `blueocean-dashboard` module (actually a plugin). Also take a look at: https://github.com/cloudbees/blueocean-sample-pipeline-result-ext-plugin for an example of an plugin that augments Blue Ocean.

Run in this directory: 

`mvn hpi:run`

    
## Verify it's working

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/

Should print:

    {"name":"jenkins"}          
    
Why not also try: `curl http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/`    

Load it in browser to see your ui code:

    http://localhost:8080/jenkins/blue
    
Load it in the browser to see the embedded Jenkins: 

    http://localhost:8080/jenkins
    
Look in blueocean-dashboard plugin for instructions on how to make changes to GUI code.    
    
    


    

    
