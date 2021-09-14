package com.pinkstack.facefinish

import org.openqa.selenium.By

implicit class LocatorHelper(private val sc: StringContext) extends AnyVal {
  def xpath(args: Any*): By = {
    val strings = sc.parts.iterator
    val expressions = args.iterator
    var buf = new StringBuilder(strings.next())
    while (strings.hasNext) {
      buf.append(expressions.next())
      buf.append(strings.next())
    }
    By.xpath(buf.toString())
  }
}
