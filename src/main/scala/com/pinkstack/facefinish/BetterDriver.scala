package com.pinkstack.facefinish

import cats.data.*
import cats.effect.IO
import cats.effect.kernel.Resource
import org.openqa.selenium.{By, OutputType, WebElement}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

import java.io.{BufferedOutputStream, FileOutputStream}
import java.time.Duration

class BetterDriver(val driver: RemoteWebDriver) {

  import collection.JavaConverters._

  def findAnd[R](f: WebElement => R)(locator: By) =
    Option(driver.findElement(locator)).map(f)

  def findAndSendKeys(locator: By, keys: String) = findAnd(_.sendKeys(keys))(locator)

  def findAndClick(locator: By) = findAnd(_.click())(locator)

  def waitUntil[R, C](expectedCondition: ExpectedCondition[C],
                      defaultDuration: Duration = Duration.ofSeconds(15)) =
    new WebDriverWait(driver, defaultDuration).until(expectedCondition)

  def waitAnd[R, C](locator: By,
                    expectedCondition: ExpectedCondition[C],
                    defaultDuration: Duration = Duration.ofSeconds(15))(f: WebElement => R) = {
    waitUntil(expectedCondition, defaultDuration)
    findAnd(f)(locator)
  }

  def waitAndClick(locator: By,
                   defaultDuration: Duration = Duration.ofSeconds(15)) =
    waitAnd(locator, ExpectedConditions.elementToBeClickable(locator), defaultDuration)(_.click())

  def map[R](locator: By)(f: WebElement => R) =
    driver.findElements(locator).asScala.map(f)

  def forEach[R <: Unit](locator: By)(f: WebElement => Unit) =
    map(locator)(f)

  def takeScreenshot(name: String) = {
    val shot = driver.getScreenshotAs[Array[Byte]](OutputType.BYTES)
    val stream = new BufferedOutputStream(new FileOutputStream(name))
    stream.write(shot)
    stream.close()
  }
}

object BetterDriver {
  given Conversion[RemoteWebDriver, BetterDriver] = new BetterDriver(_)
}
