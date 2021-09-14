package com.facefinish

import org.openqa.selenium.{JavascriptExecutor, PageLoadStrategy}
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.*
import org.openqa.selenium.chrome.*

import java.time.Duration
import java.net.URL
import java.util.Collections
import java.util.logging.Level
import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}

import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util
import java.util.concurrent.TimeUnit

object FacefinishApp extends IOApp.Simple with LazyLogging :

  import collection.JavaConverters._
  import BetterDriver.given_Conversion_RemoteWebDriver_BetterDriver

  def mkDriver(configuration: Configuration): Resource[IO, RemoteWebDriver] =
    Resource.make(IO(Browser.getDriver(configuration))) { driver =>
      IO(logger.info("Quitting driver")) *> IO(driver.quit())
    }

  val runSimple =
    for
      config <- IO.fromEither(Configuration.load)
      pom <- mkDriver(config).use { (driver: RemoteWebDriver) =>
        import java.io.{File}

        // driver.manage.timeouts.implicitlyWait(Duration.ofSeconds(120))
        // driver.manage.timeouts.pageLoadTimeout(Duration.ofSeconds(360))

        // val title = driver.get("https://pbs.twimg.com/profile_images/1323233424237301765/EMxn2Cvb_400x400.jpg")
        // val title = driver.get("https://twitter.com/otobrglez")
        val title = driver.get("https://whatismyipaddress.com/")

        new WebDriverWait(driver, Duration.ofSeconds(30))
          .until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@href='/otobrglez/header_photo']")))


        // driver.waitX(
        //   By.xpath("//a[@href='/otobrglez/header_photo']"),
        //   ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/otobrglez/header_photo']"))
        // ))

        val screenshot = driver.getScreenshotAs[Array[Byte]](OutputType.BYTES)
        val stream = new FileOutputStream("screenshot.png")
        stream.write(screenshot)
        stream.flush()
        stream.close()

        IO(title)
      }
      _ <- IO.println(s"Hello ${pom}")
    yield ()

  val runFB =
    for {
      config <- IO.fromEither(Configuration.load)
      title <- mkDriver(config).use { (driver: RemoteWebDriver) =>
        driver.manage().window().maximize()
        driver.get("https://m.facebook.com")

        // Accept Cookies
        driver.forEach(By.xpath("//*[@data-cookiebanner='accept_button']"))(_.click())

        // Fill the login form.
        driver.findAndSendKeys(By.name("email"), config.botEmail)
        driver.findAndSendKeys(By.name("pass"), config.botPassword)
        driver.findAndClick(By.name("login"))

        // Click the "not now" for one-tap
        driver.waitAndClick(By.xpath("//a[starts-with(@href,'/login/save-device')]"))

        // Wait for "More" to appear.
        driver.waitAndClick(By.xpath("//a[@name='More']"))

        // Open specific group
        driver.waitAndClick(
          By.xpath(s"//a[starts-with(@href,'https://m.facebook.com/groups/${config.groupSlug}')]"))

        // Permalinks
        new WebDriverWait(driver, Duration.ofSeconds(15))
          .until(ExpectedConditions.elementToBeClickable(By.xpath(s"//a[contains(@href,'permalink/')]")));

        val links: Set[String] = driver.map(By.xpath("//a[contains(@href,'permalink/')]"))(
          _.getAttribute("href").split("""\/\?""").head
        ).toSet

        links.take(3).foreach { link =>
          logger.info(s"Opening ${link}")
          driver.get(link)
          driver.executeScript("""window.scrollTo(0,document.body.scrollHeight);""")

          new WebDriverWait(driver, Duration.ofSeconds(2))
            .until(ExpectedConditions.elementToBeClickable(
              By.xpath("//a[@data-sigil='MBackNavBarClick']")))

          driver.findAndClick(By.xpath("//a[@data-sigil='MBackNavBarClick']"))
        }

        IO(links)
      }
      _ <- IO.println(s"Title ${title}")

    } yield ()


  val run = runFB
