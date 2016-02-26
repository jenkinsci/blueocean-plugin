# UI Plugin

This is the main UI guts of blueocean. Javascript magic happens here as well as some core web classes. 
Look for blueocean.js for excitement. 

# Running Blue Ocean in development

## Firstly build all modules from root

    cd .. 
    mvn clean install
    
## Prepare web module for development

To hack on blue ocean UI, you will want to run it embedded in a Jenkins you can add jobs to and so on. 
To do this, you need to have it install the blueocean-plugin: 

    cd blueocean-web
    mvn clean install -Pdev
    
YOU ONLY NEED TO DO THIS ONCE (generally, maybe again if there is an important fix to blueocean-plugin).    

> to skip tests run the above command with -DskipTests options    
     
## Run it in plugin dev mode

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
$ gulp rebundle
```
(or run gulp, after each change) in the core directory. This will pick up source changes to commonjs modules (and other things) and put them in target for you (running gulp will run js unit tests too).


    

    
