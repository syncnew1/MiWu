package miwu.device

import miwu.annotation.Device
import miwu.annotation.Widgets
import miwu.support.base.MiwuDevice
import miwu.widget.StatusText
import miwu.widget.Text
import miwu.widget.VacuumButton

@Device("laptop")
@Widgets(
    StatusText::class,
    Text::class,
    VacuumButton::class,
)
class Laptop : MiwuDevice()
