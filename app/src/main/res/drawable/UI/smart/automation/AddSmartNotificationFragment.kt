package com.example.rogosample.UI.smart.automation

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.`object`.AutomationType
import com.example.rogosample.R
import com.example.rogosample.adapter.AutomationTypeAdapter
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartNotificationBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTCondition
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTReverseOnOff
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartNotification

class AddSmartNotificationFragment : BaseFragment<FragmentAddSmartNotificationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_notification

    private var condition = -1
    private lateinit var ioTSmartNotification: IoTSmartNotification

    private val hourFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until  23).toList())
    }
    private val minuteFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val hourSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 23).toList())
    }
    private val minuteSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val timeAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val deviceSpinnerAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.filter {
            getSupportedDevice().contains(it.devType)
        })
    }

    private lateinit var automationTypeAdapter: AutomationTypeAdapter

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_notification)
            spinnerDevice.adapter = deviceSpinnerAdapter
            spinnerFirstHour.adapter = hourFirstAdapter
            spinnerFirstMinute.adapter = minuteFirstAdapter
            spinnerSecondHour.adapter = hourSecondAdapter
            spinnerSecondMinute.adapter = minuteSecondAdapter
            spinnerTime.adapter = timeAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * After selecting a device, show list of automation that supports the device
            * */
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        automationTypeAdapter = AutomationTypeAdapter(
                            requireContext(),
                            AutomationType.getAutomationTypeList().filter { type ->
                                (it.getItemAtPosition(p2) as IoTDevice).containtFeature(type.automationAttr)
                            })
                        spinnerState.adapter = automationTypeAdapter
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

            btnAddSmart.setOnClickListener {
                /*
                * Create a Smart Automation
                * */
                dialogLoading.show()
                SmartSdk.smartFeatureHandler().createSmartAutomation(
                    edtSmartName.text.toString(),
                    IoTAutomationType.TYPE_NOTIFICATION,
                    intArrayOf(),
                    null,
                    object : RequestCallback<IoTSmart> {
                        override fun onSuccess(p0: IoTSmart?) {
                            /*
                            * Bind Smart Notification to Smart Automation
                            * */
                            ioTSmartNotification = IoTSmartNotification()
                            ioTSmartNotification.devId =
                                (spinnerDevice.selectedItem as IoTDevice).uuid
                            ioTSmartNotification.smartId = p0?.uuid
                            ioTSmartNotification.minTime = (spinnerTime.selectedItem as Int)
                            ioTSmartNotification.timeJob = intArrayOf(
                                (spinnerFirstHour.selectedItem as Int) * 60 + (spinnerFirstMinute.selectedItem as Int),
                                (spinnerSecondHour.selectedItem as Int) * 60 + (spinnerSecondMinute.selectedItem as Int)
                            )
                            ioTSmartNotification.condition = getConditionByType()
                            ioTSmartNotification.elm =
                                (spinnerDevice.selectedItem as IoTDevice).elementIds.first()
                            if ((spinnerState.selectedItem as AutomationType).automationType != null) {
                                ioTSmartNotification.value = intArrayOf(
                                    (spinnerState.selectedItem as AutomationType).automationAttr,
                                    (spinnerState.selectedItem as AutomationType).automationType!!
                                )
                            } else {
                                ioTSmartNotification.value = intArrayOf(
                                    (spinnerState.selectedItem as AutomationType).automationAttr
                                )
                            }
                            SmartSdk.smartFeatureHandler().bindSmartFeatureNotification(
                                ioTSmartNotification,
                                object : RequestCallback<IoTSmartNotification> {
                                    override fun onSuccess(p0: IoTSmartNotification?) {
                                        dialogLoading.dismiss()
                                        showNoti(R.string.add_success)
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {
                                        dialogLoading.dismiss()
                                        p1?.let {
                                            showNoti(it)
                                        }
                                    }

                                }
                            )
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            dialogLoading.dismiss()
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    }
                )
            }
        }
    }

    fun getConditionByType(): Int {
        if (condition == -1) {
            // actions list size =1
            val state = (binding.spinnerState.selectedItem as AutomationType)
            if (state == AutomationType.ON_OFF ||
                state == AutomationType.OPEN_CLOSE ||
                state == AutomationType.ALL_PRESS_BUTTON ||
                state == AutomationType.MOUNT_UNMOUNT ||
                state == AutomationType.LOCK_UNLOCK ||
                state == AutomationType.PRESENCE_UNPRESENCE||
                state == AutomationType.BOTH_MOTION
            ) {
                condition = IoTCondition.ANY
                return condition
            }
            if (
                state == AutomationType.SINGLE_PRESS_BUTTON ||
                state == AutomationType.LONG_PRESS_BUTTON ||
                state == AutomationType.DOUBLE_PRESS_BUTTON
            ) {
                condition = IoTCondition.EQUAL
                return condition
            }


            when ((binding.spinnerDevice.selectedItem as IoTDevice).devType) {
                IoTDeviceType.DOOR_SENSOR,
                IoTDeviceType.PLUG,
                IoTDeviceType.AC,
                IoTDeviceType.MOTION_SENSOR,
                IoTDeviceType.DOORLOCK,
                IoTDeviceType.SMOKE_SENSOR,
                IoTDeviceType.SWITCH -> {
                    condition = IoTCondition.EQUAL
                    return condition
                }

                IoTDeviceType.MOTION_LUX_SENSOR -> {
                    if (state == AutomationType.MOTION_DETECTED || state == AutomationType.MOTION_UNDETECTED) {
                        condition = IoTCondition.EQUAL
                        return condition
                    }
                    // lux
                }

                IoTDeviceType.PRESENSCE_SENSOR -> {
                    if (state == AutomationType.PRESENCE_DETECTED || state == AutomationType.PRESENCE_UNDETECTED) {
                        condition = IoTCondition.EQUAL
                        return condition
                    }
                }
                IoTDeviceType.GATE -> {
                    if (state == AutomationType.OPEN || state == AutomationType.CLOSE) {
                        condition = IoTCondition.EQUAL
                        return condition
                    }
                }
            }
        }
        return condition
    }

    private fun getSupportedDevice() = listOf<Int>(
        IoTDeviceType.DOOR_SENSOR,
        IoTDeviceType.PLUG,
        IoTDeviceType.AC,
        IoTDeviceType.MOTION_SENSOR,
        IoTDeviceType.DOORLOCK,
        IoTDeviceType.SWITCH,
        IoTDeviceType.MOTION_LUX_SENSOR,
        IoTDeviceType.PRESENSCE_SENSOR
    )
}