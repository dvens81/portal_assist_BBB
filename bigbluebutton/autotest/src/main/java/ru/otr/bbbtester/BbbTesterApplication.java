package ru.otr.bbbtester;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;

@SpringBootApplication
public class BbbTesterApplication {
    private static WebDriver driver;
    private static String user, bbbUrl, driverName;
    private static int count;
    private static int startInterval, stopInterval, workTime, waitForElement;
    private static ArrayList<String> tabs;
    private static LocalDateTime start;

    private static void loadPage(String user, long userNum) throws InterruptedException {
        String userName = user + userNum;
        if (userNum > 0) {
            ((JavascriptExecutor) driver).executeScript("window.open()");
            tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(tabs.size() - 1));
        }
        driver.get(bbbUrl);
        WebElement toolbox = driver.findElement(By.cssSelector(".new-toolbox"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.position='static';", toolbox);
        sleep(2000);
        driver.findElement(By.cssSelector(".audio-preview .toolbox-button")).click();
    }

    public static void main(String[] args) throws InterruptedException {
        loadVariables();
        LocalDateTime time = LocalDateTime.now();
        int delay = (start.getHour() - time.getHour()) * 3600000;
        delay += (start.getMinute() - time.getMinute()) * 60000;
        delay += (start.getSecond() - time.getSecond()) * 1000;
        if(delay>0) {
            System.out.println("Waiting " + delay/1000 + "  sec ");
            Thread.sleep(delay);
            System.out.println("Start....");

        }
        else {
            System.out.println("Start time " + start+" has passed, start is not slow)))");
        }

        SpringApplication.run(BbbTesterApplication.class, args);
        Flux<String> intervalFlux1 = Flux
                .interval(Duration.ofMillis(startInterval))
                .map(tick -> {
                    if (tick < count) {
                        try {
                            loadPage(user, tick);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return "---===>>> loading " + user + tick;
                    }
                    return "All users are logged in.";
                });

        intervalFlux1.take(count).subscribe(System.out::println);
        long w = ((startInterval) * count + workTime);
        sleep(w);
        System.out.println("Closing connections");
        Flux<String> f = Flux.fromIterable(tabs)
                .map(page -> {
                    WebDriver window = driver.switchTo().window(page);
                    try {
                        sleep(stopInterval);
                        window.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return page;
                });
        f.subscribe(System.out::println);
    }

    private static void loadVariables() {
        File configFile = new File("C:\\Projects\\portal_assist\\bigbluebutton\\autotest\\jitsi-config.cfg");
        try {
            if (!configFile.exists()) {
                throw new RuntimeException("no configuration file");
            } else {
                Stream<String> lines = Files.lines(configFile.toPath());
                lines.filter(l -> !l.isEmpty()).forEach(l -> {

                    String[] split = l.split("#");
                    String[] ss = split[0].split("=");
                    if(ss.length!=2)
                        throw new RuntimeException("invalid jitsi-config.cfg file structure");

                    String ss0 = ss[0].trim();
                    String ss1 = ss[1].trim();

                    switch (ss0) {
                        case "user":
                            user = ss1;
                            break;
                        case "count":
                            count = Integer.parseInt(ss1);
                            break;
                        case "startInterval":
                            startInterval = Integer.parseInt(ss1);
                            break;
                        case "stopInterval":
                            stopInterval = Integer.parseInt(ss1);
                            break;
                        case "workTime":
                            workTime = Integer.parseInt(ss1);
                            break;
                        case "waitForElement":
                            waitForElement = Integer.parseInt(ss1);
                            break;
                        case "host":
                            bbbUrl = ss1;
                            break;
                        case "driver":
                            driverName = ss1;
                            //System.setProperty("webdriver.chrome.driver", "webdriver/" + driverName);
                            ChromeOptions options = new ChromeOptions();
                            options.addArguments("-ignore-certificate-errors");
                            options.addArguments("--use-fake-device-for-media-stream --use-file-for-fake-video-capture=C:\\Download\\ffmpeg\\bin\\output3.mjpeg");
                            options.addArguments("use-fake-ui-for-media-stream");
                            //options.addArguments("--headless");
                            //options.addArguments("use-fake-device-for-media-stream");
                            driver = new ChromeDriver(options);
                            driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
                            driver.manage().window().maximize();

                            break;
                        case "start":
                            String[] split1 = ss1.split(":");
                            LocalDateTime time = LocalDateTime.now();
                            int year = time.getYear();
                            Month month = time.getMonth();
                            int dayOfMonth = time.getDayOfMonth();
                            start = LocalDateTime.of(year, month, dayOfMonth, Integer.parseInt(split1[0]), Integer.parseInt(split1[1]));
                            break;
                        default:
                            throw new RuntimeException("unknown variable " + ss0);
                    }
                });
                System.out.println("++++++++++++++++++++++++++++++++++++++++++");

                System.out.println("driver   = " + driverName);
                System.out.println("start    = " + start);
                System.out.println("user     = " + user);
                System.out.println("count    = " + count);
                System.out.println("host     = " + bbbUrl);
                System.out.println("startInterval = " + startInterval);
                System.out.println("stopInterval  = " + stopInterval);
                System.out.println("waitForElement  = " + waitForElement + " sec");
                System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void onDestroy() {
        driver.quit();
    }
}
