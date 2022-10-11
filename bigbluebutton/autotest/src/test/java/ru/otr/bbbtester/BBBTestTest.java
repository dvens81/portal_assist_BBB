package ru.otr.bbbtester;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
public class BBBTestTest {
    private WebDriver driver;
    private Map<String, Object> vars;
    JavascriptExecutor js;
//    @Before
//    public void setUp() throws MalformedURLException {
//        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome());
//        js = (JavascriptExecutor) driver;
//        vars = new HashMap<String, Object>();
//    }
//    @After
//    public void tearDown() {
//        driver.quit();
//    }
//    @Test
    public void bBBTest() throws MalformedURLException, InterruptedException {
        System.setProperty("webdriver.chrome.driver", "webdriver/chromedriver");

        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
        // Test name: BBBTest
        // Step # | name | target | value
        // 1 | open | / |
        driver.get("http://elk-bbbub.otr.ru/");
        // 2 | setWindowSize | 1599x899 |
        driver.manage().window().setSize(new Dimension(1599, 899));
        // 3 | type | id=username | 123
        driver.findElement(By.id("username")).sendKeys("123");
        // 4 | click | css=.submit_btn |
        driver.findElement(By.cssSelector(".submit_btn")).click();
        // 5 | click | css=.icon-bbb-listen |
        Thread.sleep(30000);

        driver.findElement(By.cssSelector(".icon-bbb-listen")).click();
        driver.quit();
    }
}