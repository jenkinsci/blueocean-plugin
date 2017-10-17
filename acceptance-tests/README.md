# Acceptance tests for Blue Ocean

## System Requirements

### Operating Systems

Linux and macOS are currently the only supported OS. Windows suport is not available at this time.

### Dependancies

`libxml2-utils` and `libssl-dev` are needed by the node packages.

Maven 3.3+ and JDK8 are required to run the tests.

## Acceptance Tests

Please make sure that BlueOcean plugins are built correctly. If _funky_ things seem to be happening in ATH, try running the following command from the top level blueocean directory

```bash
mvn clean install -DskipTests -DcleanNode
```

### Areas covered

* Karaoke parallel: ParallelNavigationTest. This checks that users can navigate between concurrently executing parallel branches, including steps waiting for input.
* Commit Messages: CommitMessagesTest checks to see that commit messsages of git commits are shown on a run
* Folder Names: FoldersTest checks that folders with sperial characters work correctly in blueoean
* Github integration: GithubCreationTests cover github integration testing of buth the creation flow and also simple editor round tripping.

### Run all tests (in one command)
 ```bash
./run.sh
```

This is mainly for CI servers. It starts the selenium docker container and runs all 
nightwatch and java ATH tests in one shot.

### Run tests in DEV mode

DEV mode starts Jenkins and the selenium docker container running in a standalone mode.
This allows for individual tests to be run against the server multple times, which makes
writing ATH tests much easier.

First, start the server:
```bash
./run.sh --dev
```

Next run the desired tests

#### Java Webdriver Tests via Maven

Maven has 5 profiles set up for running tests. The default profile runs the offline tests.
The `live` profile runs tests that need services like GitHub, and the `all` profile will run all of them. Finally there 
are the `nightwatch` and `offlineAll` profiles which run the nightwatch tests and run the nightwatch  + offline tests respectively.

Specific tests can be specified with `-Dtest=`.
```bash
mvn clean test
mvn clean test -Dprofile=live
mvn clean test -Dprofile=offlineAll
mvn clean test -Dprofile=all
mvn clean test -Dprofile=all -Dtest=FavoritesTest
```

Note to run the live tests there needs to be a `live.properties` file in the acceptance-tests directory.

```properties
github.repo=<name of repository to be created
github.org=<org or user name to create repo in.>
github.token=<personal access token (roles: repo, user:email, and optionally delete_repo)>  
github.deleteRepo=<true/false should the code delete repo once test is done>
github.randomSuffix=<true/false - add a random suffix to repo name (ie must have for CI> 
```

#### Java Webdriver Tests via Intellij (and probably other ides)

Running tests via the IDE works as expected as long as the standalone part of the ATH is running.

To start a test in Intellij the easiest way is to right click on the test class or method and click Run test.

#### JavaScript [nightwatch] tests.

Nightwatch tests can be run via the nightwatch npm package (`npm install -g nightwatch`). It is
possible to specify specific test files, or whole directories which it will recursively find tests in.

```bash
nightwatch src/test/js/
nightwatch src/test/js/edgeCases/
nightwatch src/test/js/edgeCases/folder.js
```


When running in `--dev` mode, it can be useful to turn on client code log output. To do this, simply set
the `LOG_CONFIG` env variable e.g. to turn on SSE logging:

```bash
export LOG_CONFIG=sse
nightwatch src/test/js/
```

## Writing Tests

### Java Webdriver tests

#### Intellij

Make sure you add the acceptance-tests to your intellij blueocean project by right-clicking on `acceptance-tests/pom.xml`
and adding it to the project.

#### JUnit4 Tests

All test code is uses Guice to do dependency injection.

```java
//Makes the browser login at the start of every test in this class.
@Login
// Sets up the Guice DI and creates the WebDriver istan
@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class MyFirstATHTest{
    
    // Base url for the browser to naviate to. e.g driver.get(baseUrl + "/blue/")
    @Inject @BaseUrl
    String baseUrl;
    
    // Helper functions for webdriver
    @Inject 
    WaitUtil wait;
    
    // Incase direct access to the driver instance is needed
    @Inject
    Webdriver driver;
    
    // Page objects are injectable too
    @Inject
    DashboardPage dashboardPage;
    
    // Creates a temporary git repository to use in this test
    @Rule
    @Inject
    GitRepositoryRule repository;
    
    @Test
    public void myFirstTest() {
        dashboardPage.open();
        
        // Waits for 10s for the url to contain the string pipelines.
        wait.until(ExpectedConditions.urlContains("pipelines"));
    
        wait.until(By.cssSelector("button.some.clazz"));
            .click();
    }
}
```
#### Server Side Events

`SSEClient` is a JUnit `ExternalResource`. Once it is injected and marked as a rule on the test
it connects to the Jenkins server want waits for job events. Events are saved into a list as they happen.

Once a test is ready to check for events, `untilEvent()` can be used. `clear()` can be invoked at any time to clear out any 
messages recieved until that point in time.

```java
@Login
@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class MySecondATHTest{
    
    @Rule
    @Inject
    SSEClientRule sseClient;
    
    @Test
    public void mySecondTest() {
       // ... do something that makes a build run.
       
       // This waits for any builds that have been queued to finish.
       sseClient.untilEvents(SSEEvents.activityComplete(pipelineName));
       // Clear all events so far so that when wait is called again it doesnt see the old events.
       sseClient.clear();

       // ... some more run stuff
       sseClient.untilEvents(SSEEvents.activityComplete(pipelineName));
       
       // .. finish off test
    }
}
```
#### Page Objects

Page Objects allow for selectors and actions to be grouped into reusable classes.

```java
@Singleton
public class MyAwesomePage {
   
    @Inject
    WaitUtil wait;
    // PageFactory.initElements drivers the @Findby annatations. TBD if we want to use these TBH
    @Inject
    public MyAwesomePage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    // Use annotation to find the WebElement instead of driver.findElement
    @FindBy(xpath = "//span[text()='Awesome']")
    public WebElement myAwesomeButton;
    
    public void clickAwesomeButton() {
        // Make sure the button is visible
        wait.until(ExpectedConditions.visibilityOf(myAwesomeButton));
        myAwesomeButton.click()
        
        // Check to see if another element is selected using the By selector.
        WebElement somethingElse = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("somethingElse")));
        somethingElse.isSelected();
    }
}
``` 

Any new Page Object classes need to be bound in `ATHModule#configure` to be able to be injected via Guice.

#### Pipeline Helper

These are a series or helpers to deal with pipelines. The started life to deal with pieplines being in folders. It offes a way 
to give PageOjects more contextual information about what they are operating on without having to be explicit about it in every method call.

```java

@Login
@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class MySecondATHTest{
    
    @Inject
    MultiBranchPipelineFactory mbpFactory;  
    
    @Rule
    @Inject
    public GitRepositoryRule git;
    
    
    @Rule
    @Inject
    public SSEClientRule sseClient;
    
    @Test
    public void mySecondTest() {
        // Sets up the pipeline model with folders and a job name
        MultiBranchPipeline pipeline = mbpFactory.pipeline(Folder.folders("afolder", "bFolder"), "pipelineName");
        
        // Creates the pipeline in jenkins using the git repository rule.
        pipeline.createPipeline(git);
        
        // Wait for all runs to finish on the pipeline. Including all branches.
        sseClient.untilEvents(pipeline.buildsFinished);
        
        // Builds a bunch
        pipeline.buildBranch("master");
        
        // Opens the activity page for this pipeline and verifies its not 404.
        pipeline.getActivityPage().open();
        // Stops any runsa that are currently running on any branch job.
        pipeline.stopAllBuilds();
        
    }
}
```

###
[nightwatch]: http://nightwatchjs.org/
