package miwu.widget

import miwu.annotation.*
import miwu.annotation.widget.*
import miwu.support.base.MiwuWidget

@Widget
@Header
@Bind<Property>("environment", "status")
@Bind<Property>("vacuum", "mode")
@Bind<Property>("curtain", "status")
@Bind<Property>("gas-sensor", "status")
@Bind<Property>("laptop", "status")
@Bind<Property>("battery", "charging-state")
class StatusText : MiwuWidget<Int>()