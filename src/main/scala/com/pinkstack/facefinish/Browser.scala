package com.pinkstack.facefinish

import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.remote.RemoteWebDriver

import java.util
import java.util.Collections
import java.util.logging.Level

object Browser:
  def getDriver(configuration: Configuration): RemoteWebDriver =
    val options = new ChromeOptions()
    options.addArguments("excludeSwitches")
    options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"))
    options.setExperimentalOption("useAutomationExtension", false)

    val prefs = new util.HashMap[String, Any]();
    prefs.put("credentials_enable_service", false)
    prefs.put("profile.password_manager_enabled", false);

    options.setExperimentalOption("prefs", prefs)
    options.addArguments(
      // "--silent",
      // "--blink-settings=imagesEnabled=false",
      // "--window-size=1920,1200",
      "--disable-blink-features",
      "--disable-blink-features=AutomationControlled",
      "--disable-notifications",
      "--disable-gpu",
      "--no-sandbox",
      "--ignore-certificate-errors",
      "--disable-application-cache",
      "--no-default-browser-check",
      "--disable-web-security",
      "--allow-running-insecure-content",
      "--allow-insecure-localhost",
      "--ignore-certificate-errors",
      "--ignore-urlfetcher-cert-requests",
      "--disable-single-click-autofill",
      "--disable-popup-blocking",
      "--disable-extensions",
      "--start-maximized",
      s"""--proxy="http=${configuration.proxy};https=${configuration.proxy}"""",
      "--proxy-bypass-list=localhost",
      "--ignore-ssl-errors=true",
      "--ssl-protocol=any"
    )

    val logPrefs = new LoggingPreferences
    logPrefs.enable(LogType.PERFORMANCE, Level.ALL)
    logPrefs.enable(LogType.BROWSER, Level.ALL)

    options.setCapability("goog:loggingPrefs", logPrefs)
    options.setPageLoadStrategy(PageLoadStrategy.NORMAL)
    options.setCapability("w3c", false)
    options.setExperimentalOption("w3c", false)
    options.setAcceptInsecureCerts(true)

    // Proxy
    val proxy = new org.openqa.selenium.Proxy()
    proxy.setAutodetect(false)
    proxy.setHttpProxy(configuration.proxy)
    proxy.setSslProxy(configuration.proxy)
    proxy.setNoProxy("no_proxy-var")

    options.setProxy(proxy)
    options.setCapability("proxy", proxy)

    new RemoteWebDriver(configuration.seleniumHub, options)
