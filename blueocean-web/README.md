# UI Plugin

This is the main UI guts of blueocean. Javascript magic happens here as well as some core web classes. 
Look for blueocean.js for excitement. 

# Running Blue Ocean in development

If you want to add to some extension points, take a look at the `blueocean-dashboard` module (actually a plugin) for an example.

## Firstly build all modules from root

    cd .. 
    mvn clean install
    
    
## Run it in plugin dev mode

To hack on blue ocean UI, you will want to run it embedded in a Jenkins you can add jobs to and so on. 
To do this, use the `blueocean-plugin` module: 

    cd blueocean-plugin
    mvn hpi:run
    
## Verify it's working

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/

Should print:

    {"name":"jenkins"}          
    
Why not also try: `curl http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/`    

Load it in browser to see your ui code:

    http://localhost:8080/jenkins/blue
    
Load it in the browser to see the embedded Jenkins: 

    http://localhost:8080/jenkins
    
    
## Javascript and HTML development

Jenkins-js-modules and friends are used to power this. Look in `src/main/js`. 

If you wish to make changes to blueocean.js, then you will need to install gulp (http://gulpjs.com/), and then run (in a separate terminal to mvn hpi:run):

```
$ cd blueocean-web
$ gulp bundle:watch
```

(or run gulp, after each change) in the `blueocean-web` directory. This will pick up any changes. 
If you are editing any other UI modules, run the same in their respective directories. 


    

    
