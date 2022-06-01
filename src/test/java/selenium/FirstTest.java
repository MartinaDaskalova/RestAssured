package selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FirstTest {
    public static void main(String[] args) throws InterruptedException {

        //System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");

        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = new ChromeDriver();
        driver.get("http://training.skillo-bg.com/posts/all");
        driver.manage().window().maximize();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        // test
        WebElement loginButton = driver.findElement(By.id("nav-link-login"));
        WebElement passwordField = driver.findElement(By.id("defaultLoginFormPassword"));
        WebElement userNameField = driver.findElement((By.id("defaultLoginFormUsername")));
        userNameField.sendKeys("carpan1@abv.bg");
        passwordField.sendKeys("A931125z");
        WebElement signInButton = driver.findElement(By.id("sign-in-button"));
        signInButton.click();
        WebElement newPostButton = driver.findElement(By.id("nav-link-new-post"));
        Assert.assertTrue(newPostButton.isDisplayed());

        WebElement loginButton1 = driver.findElement(By.xpath("//*[@id='nav-link-login'"));
        //List<WebElement> loginButtons = driver.findElements(By.id("nav-link-login"));
        loginButton.click();


        WebElement homeButton = driver.findElement(By.linkText("Home"));
        Thread.sleep(5000);
        homeButton.click();


        //bad practice - use waits
        Thread.sleep(5000);
        driver.close();
    }
}
