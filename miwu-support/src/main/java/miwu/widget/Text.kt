package miwu.widget

import miwu.annotation.*
import miwu.annotation.widget.*
import miwu.support.base.MiwuWidget

@Widget
@Header
@Bind<Property>("temperature-humidity-sensor", "temperature")
@Bind<Property>("temperature-humidity-sensor", "relative-humidity")
@Bind<Property>("environment", "relative-humidity")
@Bind<Property>("environment", "pm2.5-density")
@Bind<Property>("gas-sensor", "gas-concentration")
@Bind<Property>("cpu-management", "temperature")
@Bind<Property>("battery", "battery-level")
class Text : MiwuWidget<String>()