package com.pinkstack.facefinish

import cats._
import cats.implicits._
import java.net.URL

final case class Configuration(botEmail: String,
                               botPassword: String,
                               seleniumHub: URL,
                               proxy: String,
                               groupSlug: String)

object Configuration:
  def load: Either[Throwable, Configuration] =
    for
      botEmail <- loadEnv("BOT_EMAIL")
      botPassword <- loadEnv("BOT_PASSWORD")
      seleniumHubUrl <- loadEnv("SELENIUM_HUB_URL").map(new URL(_))
      proxy <- loadEnv("HTTP_PROXY").orElse(Right("vpn:8118"))
      groupSlug <- Right("developerji")
    yield Configuration(botEmail, botPassword, seleniumHubUrl, proxy, groupSlug)

  private val loadEnv: String => Either[Throwable, String] = key =>
    sys.env.get(key).toRight(new Exception(s"Missing key ${key}"))

