package miwu.android.wrapper.common

import android.content.Context
import miwu.android.R
import miwu.android.databinding.MiotWidgetListButtonBinding
import miwu.android.wrapper.base.MiwuWrapper
import miwu.annotation.Wrapper
import miwu.icon.NoneIcon
import miwu.support.base.MiwuWidget
import miwu.widget.ModeButton

@Wrapper(ModeButton::class)
class ModeButtonWrapper(context: Context, widget: MiwuWidget<Int>) : MiwuWrapper<Int>(context, widget) {

    private val binding by viewBinding(MiotWidgetListButtonBinding::inflate)
    override val view get() = binding.root
    override val onClickView get() = binding.on

    override fun onUpdateValue(value: Int) {
        if (value == defaultValue) {
            binding.on.setBackgroundResource(R.drawable.bg_item_blue)
        } else {
            binding.on.setBackgroundResource(R.drawable.bg_item)
        }
    }

    override fun initWrapper() {
        val desc = descriptionTranslation
        if (icon is NoneIcon) {
            binding.on.setImageResource(resolveFallbackIcon(desc))
        } else {
            binding.on.setIcon(icon)
        }
        binding.desc.text = desc
    }

    override fun onClick() {
        update(defaultValue)
    }

    private fun resolveFallbackIcon(desc: String): Int {
        val text = desc.lowercase()
        return when {
            text.contains("睡") || text.contains("sleep") -> R.drawable.ic_sleep
            text.contains("唤醒") || text.contains("开机") || text.contains("启动") || text.contains("wake") || text.contains("start") -> R.drawable.ic_power
            text.contains("关机") || text.contains("关闭") || text.contains("shutdown") || text.contains("off") -> R.drawable.ic_pause_round
            text.contains("自动") || text.contains("auto") -> R.drawable.ic_auto
            text.contains("制冷") || text.contains("cool") -> R.drawable.ic_cool
            text.contains("制热") || text.contains("heat") -> R.drawable.ic_heat
            text.contains("除湿") || text.contains("dry") -> R.drawable.ic_dry
            text.contains("风") || text.contains("fan") -> R.drawable.ic_fan
            text.contains("摇") || text.contains("shake") -> R.drawable.ic_shake
            text.contains("清") || text.contains("clean") -> R.drawable.ic_clean
            else -> R.drawable.ic_mode
        }
    }
}
