package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DashboardPage {
    @Inject
    WebDriver driver;

    @Inject @BaseUrl
    String base;

    @Inject
    public DashboardPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void open() {
        driver.get(base + "/blue/");
    }

    public boolean isFavorite(String job) {
        WebElement tr = driver.findElement(By.xpath("//tr[@data-name=\"" + job + "\"]"));
        WebElement favorite = tr.findElement(By.cssSelector(".Checkbox.Favorite > label > input"));
        return favorite.isSelected();
    }
    public void toggleFavorite(String job) {
        WebElement tr = driver.findElement(By.xpath("//tr[@data-name=\"" + job + "\"]"));
        WebElement favorite = tr.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        favorite.click();
    }

}
