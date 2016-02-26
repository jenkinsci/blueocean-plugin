# UI Development Process

## Build all modules from root

    mvn clean install
    
## Build web module and copy necessary hpi files to work/plugins folder

You typically build only once or unless needed to compile java class or update other blueocean plugins 
     
    mvn clean install -Pdev

> to skip tests run the above command with -DskipTests options    
     
## Run

    mvn hpi:run

## Verify

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/

Should print:

    {"name":"jenkins"}          

Load it in browser to see your ui code:

    http://localhost:8080/jenkins/blue
