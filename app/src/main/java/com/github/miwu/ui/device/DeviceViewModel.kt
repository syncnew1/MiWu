package com.github.miwu.ui.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.miwu.logic.repository.LocalRepository
import com.github.miwu.utils.Logger
import com.github.miwu.utils.MiotDeviceClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import miwu.android.icon.generated.icon.AndroidIcons
import miwu.android.translate.AndroidTranslateHelper
import miwu.miot.kmp.utils.to
import miwu.miot.model.MiotUser
import miwu.miot.model.att.SpecAtt
import miwu.miot.model.miot.MiotDevice
import miwu.miot.provider.MiotSpecAttrProvider
import miwu.support.manager.MiotDeviceManager
import org.koin.core.component.KoinComponent

class DeviceViewModel(
    private val application: Application,
    private val localRepository: LocalRepository,
    private val savedStateHandle: SavedStateHandle,
    private val specAttrProvider: MiotSpecAttrProvider
) : AndroidViewModel(application), MiotDeviceManager.Callback, KoinComponent {
    private val logger = Logger()
    private val device = savedStateHandle.get<String>("device")
        ?.to<MiotDevice>()
        ?.getOrThrow()
        ?: error("MiotDevice is not found")
    private val user = savedStateHandle.get<String>("user")
        ?.to<MiotUser>()
        ?.getOrThrow()
        ?: error("MiotUser is not found")
    private val miotDeviceClient = MiotDeviceClient(user)
    private val _event = Channel<Event>()
    private val _uncontrollableReason = MutableStateFlow<String?>(null)
    val event: ReceiveChannel<Event> = _event
    val uncontrollableReason: StateFlow<String?> = _uncontrollableReason.asStateFlow()
    val isFromTile = savedStateHandle.get<Boolean>("isFromTile") ?: false
    val manager by lazy {
        MiotDeviceManager.build(
            miotDeviceClient,
            specAttrProvider,
            device,
            AndroidIcons,
            AndroidCache(application),
            AndroidTranslateHelper,
            Dispatchers.Main,
            this
        )
    }

    fun printDeviceInfo() {
        with(device) {
            logger.info(
                "Current miot device info: model={}, mac={}, did={}, isOnline={}, specType={}",
                model,
                mac,
                did,
                isOnline,
                specType,
            )
            logger.debug("Current miot all device info: {}", this)
        }
    }

    fun checkControllable() {
        viewModelScope.launch {
            val specType = device.specType
            if (specType.isNullOrBlank()) {
                _uncontrollableReason.value = "该设备未开放标准控制接口（specType 为空）"
                logger.warn(
                    "Device not controllable: specType is null, model={}, did={}",
                    device.model,
                    device.did
                )
                return@launch
            }

            val att = device.getSpecAtt(specAttrProvider).getOrNull()
            if (att == null) {
                _uncontrollableReason.value = "设备规范读取失败（可能是接口限制或鉴权失效）"
                logger.warn(
                    "Device not controllable: getSpecAtt failed, model={}, did={}, specType={}",
                    device.model,
                    device.did,
                    specType
                )
                return@launch
            }

            val writableCount = att.services.sumOf { service ->
                service.properties?.count { "write" in it.access } ?: 0
            }
            val actionCount = att.services.sumOf { service ->
                service.actions?.size ?: 0
            }

            _uncontrollableReason.value =
                if (writableCount == 0 && actionCount == 0) "该设备仅支持状态查看，未开放可写属性/动作"
                else null

            logger.info(
                "Device controllable check: model={}, did={}, specType={}, writableProps={}, actions={}",
                device.model,
                device.did,
                specType,
                writableCount,
                actionCount
            )
        }
    }

    fun getFallbackUnsupportedReason(): String {
        val model = device.model.ifBlank { "unknown" }
        val specType = device.specType?.takeIf { it.isNotBlank() } ?: "null"
        return "当前设备暂未适配\nmodel=$model\nspecType=$specType"
    }

    fun addFavorite() {
        localRepository.addDevice(device)
    }

    override fun onDeviceInitiated() {
        _event.trySend(Event.DeviceInitiated)
    }

    override fun onDeviceAttLoaded(specAtt: SpecAtt) {
        logger.info("onDeviceAttLoaded, device {}, spec att: {}", device.name, specAtt)
    }

    sealed interface Event {
        object DeviceInitiated: Event
    }
}
