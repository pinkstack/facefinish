package com.facefinish

import cats.data.*
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

import java.time.Duration

class BetterDriver(val driver: RemoteWebDriver) {
  import collection.JavaConverters._

  def findAnd[R](f: WebElement => R)(locator: By) =
    Option(driver.findElement(locator)).map(f)

  def findAndSendKeys(locator: By, keys: String) = findAnd(_.sendKeys(keys))(locator)

  def findAndClick(locator: By) = findAnd(_.click())(locator)
  
  def waitAnd[R, C](locator: By,
                    expectedCondition: ExpectedCondition[C],
                    defaultDuration: Duration = Duration.ofSeconds(15))(f: WebElement => R) = {
    new WebDriverWait(driver, defaultDuration).until(expectedCondition)
    findAnd(f)(locator)
  }

  def waitAndClick(locator: By,
                   defaultDuration: Duration = Duration.ofSeconds(15)) =
    waitAnd(locator, ExpectedConditions.elementToBeClickable(locator), defaultDuration)(_.click())

  def map[R](locator: By)(f: WebElement => R) =
    driver.findElements(locator).asScala.map(f)
  
  def forEach(locator: By)(f: WebElement => Unit) =
    map(locator)(f)
}

object BetterDriver {
  given Conversion[RemoteWebDriver, BetterDriver] = new BetterDriver(_)
}
