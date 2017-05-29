package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.MultiBranchPipeline;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import javax.inject.Inject;

public class EditorPage {
    private Logger logger = Logger.getLogger(EditorPage.class);

    private MultiBranchPipeline pipeline;

    private WebDriver driver;

    @Inject
    private WaitUtil wait;

    @Inject
    public EditorPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void simplePipeline() {
        logger.info("Editing simple pipeline");
        wait.until(By.xpath("(//*[@class='pipeline-node-hittarget'])[2]")).click();
        wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Test stage");
        wait.until(By.cssSelector("button.btn-primary.add")).click();
        wait.until(By.xpath("//*[text()='Print Message']")).click();
        wait.until(By.cssSelector("input.TextInput-control")).sendKeys("hi there");
        wait.until(By.xpath("(//span[@class='back-from-sheet'])[2]")).click();
        wait.until(By.xpath("//*[text()='Save']")).click();
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("Simple pipeline");
        wait.until(By.xpath("//*[text()=\"Save & run\"]")).click();
        logger.info("Simple pipeline saved");

    }
}
