package com.example.rogosample.UI.device.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.ElementSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentControlDeviceBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.callback.SuccessRequestCallback
import rogo.iot.module.platform.callback.SuccessStatus
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTCmdConst
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.AckStatusCallback
import rogo.iot.module.rogocore.sdk.callback.SelfTestDeviceAckCallback
import rogo.iot.module.rogocore.sdk.callback.SuccessStatusCallback
import rogo.iot.module.rogocore.sdk.define.IoTAckStatus
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTSelfTestResult
import java.util.Arrays

class ControlDeviceFragment : BaseFragment<FragmentControlDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_control_device

    private var light_sat = 0f
    private var light_hue = 0f
    private var ioTDevice: IoTDevice? = null
    private val elementMap = hashMapOf<Int, String>()
    private lateinit var elementSpinnerAdapter: ElementSpinnerAdapter
    override fun initVariable() {
        super.initVariable()
        arguments?.let {
            ioTDevice = SmartSdk.deviceHandler().get(it.getString("uuid"))
        }
        ioTDevice?.let {
            ILogR.D("ControlDeviceFragment", it.productId, it.devType, Arrays.toString(it.features))
            it.elementInfos.entries.forEach {
                ILogR.D("ControlDeviceFragment", it.key, it.value.label, it.value.devType, Arrays.toString(it.value.attrs))
            }
            ILogR.D("ControlDeviceFragment", "PRODUCT_MODEL", Gson().toJson(SmartSdk.getProductModel(it.productId)))
        }
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.control_device)
            ioTDevice?.let { it ->
                txtDeviceName.text = it.label
                elementMap.clear()
                it.elementInfos.forEach { entry ->
                    if (entry.value.label.isNullOrEmpty()) {
                        elementMap[entry.key] =
                            "Nút ${it.getIndexElement(entry.key)}"
                    } else {
                        elementMap[entry.key] = entry.value.label
                    }
                }
                elementSpinnerAdapter =
                    ElementSpinnerAdapter(requireContext(), elementMap.entries.toList())
                spinnerElement.adapter = elementSpinnerAdapter
                if(it.elementInfos.isEmpty()) {
                    lnElement.visibility = View.GONE
                } else {
                    lnElement.visibility = View.VISIBLE
                }
                if (it.containtFeature(IoTAttribute.ACT_ONOFF)) {
                    lnOnoff.visibility = View.VISIBLE
                }
                if (it.containtFeature(IoTAttribute.ACT_BRIGHTNESS)) {
                    lnBrightness.visibility = View.VISIBLE
                }
                if (it.containtFeature(IoTAttribute.ACT_OPEN_CLOSE)) {
                    lnOpenclose.visibility = View.VISIBLE
                }
                if (it.containtFeature(IoTAttribute.ACT_LOCK_UNLOCK)) {
                    lnLockUnlock.visibility = View.VISIBLE
                }
                if (it.containtFeature(IoTAttribute.ACT_COLOR_HSV)) {
                    lnSaturation.visibility = View.VISIBLE
                }
                if (it.devType == IoTDeviceType.GATEWAY) {
                    lnRfControl.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Switch device on, off
            * */
            switchOnoff.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    SmartSdk.controlHandler().controlDevicePower(
                        ioTDevice?.uuid, intArrayOf(
                            (spinnerElement.selectedItem as Map.Entry<Int, String>).key
                        ), p1, object : AckStatusCallback(3000) {
                            override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                            }
                        }
                    )
                }
            })

            /*
            * Choose open or close
            * */
            btnOpen.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    ioTDevice?.uuid,
                    false,
                    IoTCmdConst.OPENCLOSE_MODE_OPEN,
                    null
                )
            }

            btnClose.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    ioTDevice?.uuid,
                    false,
                    IoTCmdConst.OPENCLOSE_MODE_CLOSE,
                    null
                )
            }
            btnStop.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    ioTDevice?.uuid,
                    false,
                    IoTCmdConst.OPENCLOSE_MODE_STOP,
                    null
                )
            }

            /*
            * Choose lock or unlock
            * */
            btnLock.setOnClickListener {
                SmartSdk.controlHandler().controlLock(
                    ioTDevice?.uuid,
                    IoTCmdConst.DOOR_LOCKED,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {
                            TODO("Not yet implemented")
                        }
                    })
            }

            btnUnlock.setOnClickListener {
                SmartSdk.controlHandler().controlLock(
                    ioTDevice?.uuid,
                    IoTCmdConst.DOOR_UNLOCKED,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }
                    })
            }

            /*
            * Change the color, brightness of the device
            * */
            sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    SmartSdk.controlHandler().controlDim(
                        ioTDevice?.uuid, false, p1 * 10, null
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            sbSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    light_sat = p1.toFloat()
                    SmartSdk.controlHandler().controlLightHsv(
                        ioTDevice?.uuid,
                        false,
                        floatArrayOf(light_sat, light_hue, 1F),
                        null
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            sbColor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    light_hue = p1 * 0.2F
                    SmartSdk.controlHandler().controlLightHsv(
                        ioTDevice?.uuid,
                        false,
                        floatArrayOf(light_sat, light_hue, 1F),
                        null
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
            btnLocateDevicePostion.setOnClickListener {
                dialogLoading.show()
                SmartSdk.controlHandler().locatePositionDevice(
                    (ioTDevice?.uuid),
                    30,
                    object : SuccessStatus {
                        override fun onStatus(p0: Boolean) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(p0.toString())
                            }
                        }

                    }
                )
            }

            btnSilenceAlarm.setOnClickListener {
                dialogLoading.show()
                SmartSdk.controlHandler().silenceAlarm(
                    ioTDevice?.uuid,
                    object : SuccessStatus {
                        override fun onStatus(p0: Boolean) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(
                                    if (p0) R.string.success else R.string.failure
                                )
                            }
                        }

                    }
                )
            }

            btnRequestSelfTest.setOnClickListener {
                dialogLoading.show()
                SmartSdk.controlHandler().requestSelfTestDevice(
                    ioTDevice?.uuid,
                    30,
                    object : SelfTestDeviceAckCallback{
                        override fun onReplyAckSuccess() {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.success)
                            }
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.failure)
                            }
                        }
                    }
                )
            }

            btnRequestSelfTestNwk.setOnClickListener {
                dialogLoading.show()
                SmartSdk.controlHandler().requestSelfTestDeviceNwk(
                    ioTDevice?.uuid,
                    30,
                    object : RequestCallback<HashMap<String, IoTSelfTestResult>> {
                        override fun onSuccess(p0: HashMap<String, IoTSelfTestResult>?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.success)
                                p0?.let {
                                    it.forEach {
                                        ILogR.D("ControlRF", it.key, Gson().toJson(it.value))
                                    }
                                }
                            }
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dialogLoading.dismiss()
                                showNoti(R.string.failure)
                            }
                        }
                    }
                )
            }
        }
    }
}