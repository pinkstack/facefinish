package com.pinkstack.facefinish

import cats.*
import cats.data.*
import cats.effect.*
import com.typesafe.scalalogging.LazyLogging
import org.openqa.selenium.chrome.*
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.*

import java.io.FileOutputStream
import java.net.URL
import java.time.Duration
import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.logging.Level

object FacefinishApp extends IOApp.Simple with LazyLogging :

  import BetterDriver.given_Conversion_RemoteWebDriver_BetterDriver

  import collection.JavaConverters.*

  def mkDriver(configuration: Configuration): Resource[IO, RemoteWebDriver] =
    Resource.make(IO(Browser.getDriver(configuration))) { driver =>
      IO(logger.info("Quitting driver")) *> IO(driver.quit())
    }

  val runFB =
    for {
      config <- IO.fromEither(Configuration.load)
      title <- mkDriver(config).use { (driver: RemoteWebDriver) =>
        driver.manage().window().maximize()
        driver.get("https://m.facebook.com")

        // Accept Cookies
        driver.forEach(xpath"//*[@data-cookiebanner='accept_button']")(_.click())

        // Fill the login form.
        driver.findAndSendKeys(By.name("email"), config.botEmail)
        driver.findAndSendKeys(By.name("pass"), config.botPassword)
        driver.findAndClick(By.name("login"))

        // Click the "not now" for one-tap
        driver.waitAndClick(xpath"//a[starts-with(@href,'/login/save-device')]")

        // Wait for "More" to appear.
        driver.waitAndClick(xpath"//a[@name='More']")

        // Open specific group
        driver.waitAndClick(xpath"//a[starts-with(@href,'https://m.facebook.com/groups/${config.groupSlug}')]")

        // Permalinks
        new WebDriverWait(driver, Duration.ofSeconds(15))
          .until(ExpectedConditions.elementToBeClickable(xpath"//a[contains(@href,'permalink/')]"));

        val links: Set[String] = driver.map(xpath"//a[contains(@href,'permalink/')]")(
          _.getAttribute("href").split("""\/\?""").head
        ).toSet

        links.take(3).foreach { link =>
          logger.info(s"Opening ${link}")
          driver.get(link)
          driver.executeScript("""window.scrollTo(0,document.body.scrollHeight);""".stripMargin)

          driver.waitUntil(ExpectedConditions.elementToBeClickable(
            xpath"//a[@data-sigil='MBackNavBarClick']"),
            Duration.ofSeconds(2)
          )

          driver.findAndClick(xpath"//a[@data-sigil='MBackNavBarClick']")
        }

        IO(links)
      }
      _ <- IO.println(s"Title ${title}")

    } yield ()


  val run = runFB
