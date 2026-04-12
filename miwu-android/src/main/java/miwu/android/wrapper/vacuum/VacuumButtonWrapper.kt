package miwu.android.wrapper.vacuum

import android.content.Context
import android.view.View
import miwu.android.R
import miwu.android.databinding.MiotWidgetListButtonBinding
import miwu.android.wrapper.base.MiwuActionWrapper
import miwu.annotation.Wrapper
import miwu.icon.NoneIcon
import miwu.spec.Action
import miwu.spec.Property
import miwu.spec.Service
import miwu.support.base.MiwuWidget
import miwu.widget.VacuumButton

@Wrapper(VacuumButton::class)
class VacuumButtonWrapper(context: Context, widget: MiwuWidget<Unit>) :
    MiwuActionWrapper(context, widget) {
    private val binding by viewBinding(MiotWidgetListButtonBinding::inflate)
    private val status by lazy { getProperty(Service.Vacuum, Property.Mode) }
    override val view: View get() = binding.root
    override val onClickView: View get() = binding.on

    override fun initWrapper() {
        val desc = descriptionTranslation
        if (icon is NoneIcon) {
            binding.on.setImageResource(resolveFallbackIcon(actionName, desc))
        } else {
            binding.on.setIcon(icon)
        }
        binding.desc.text = desc
        register(Service.Vacuum, Property.Mode) { value ->
            if (value !is Int) return@register
            val list = status?.valueList ?: return@register
            val modeDesc = list.firstOrNull { it.value == value }?.description ?: return@register
            when (modeDesc) {
                "Sweeping", "Sweeping and Mopping", "Mopping" -> onCleaning()
                "Charging", "Go Charging" -> onCharging()
                "Upgrading" -> Unit
                else -> disable()
            }
        }
    }

    override fun onClick() {
        action()
    }

    private fun onCleaning() {
        when (actionName) {
            Action.StartCharge -> disable()
            Action.StopCharge -> enabled()
        }
    }

    private fun onCharging() {
        when (actionName) {
            Action.StopCharge -> enabled()
            Action.StartCharge -> disable()
        }
    }

    private fun enabled() {
        binding.on.setBackgroundResource(R.drawable.bg_item_blue)
    }

    private fun disable() {
        binding.on.setBackgroundResource(R.drawable.bg_item)
    }

    private fun resolveFallbackIcon(action: String, desc: String): Int {
        val actionKey = action.lowercase()
        val descKey = desc.lowercase()
        return when {
            actionKey.contains("sleep") || descKey.contains("睡") || descKey.contains("sleep") -> R.drawable.ic_sleep
            actionKey.contains("turn-on") || actionKey.contains("power-on") || actionKey.contains("start") || descKey.contains("唤醒") || descKey.contains("开机") || descKey.contains("启动") || descKey.contains("wake") -> R.drawable.ic_power
            actionKey.contains("turn-off") || actionKey.contains("power-off") || actionKey.contains("stop") || descKey.contains("关机") || descKey.contains("关闭") || descKey.contains("off") -> R.drawable.ic_pause_round
            actionKey.contains("charge") || descKey.contains("充电") || descKey.contains("charge") -> R.drawable.ic_charge
            actionKey.contains("clean") || descKey.contains("清扫") || descKey.contains("清洁") || descKey.contains("clean") -> R.drawable.ic_clean
            else -> R.drawable.ic_mode
        }
    }
}
