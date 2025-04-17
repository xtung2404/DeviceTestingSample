package com.example.rogosample.UI.smart.automation.editAutomation

import android.view.View
import android.widget.AdapterView
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.AutomationTypeAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditSmartNotificationBinding
import com.example.rogosample.`object`.AutomationType
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTCondition
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartNotification

class EditSmartNotificationFragment : BaseFragment<FragmentEditSmartNotificationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_smart_notification
    private var condition = -1
    private lateinit var ioTSmartNotification: IoTSmartNotification
    private val hourFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 12).toList())
    }
    private val minuteFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val hourSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 12).toList())
    }
    private val minuteSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val timeAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val smartSpinnerAdapter by lazy {
        SmartSpinnerAdapter(requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(
            IoTSmartType.TYPE_AUTOMATION).filter {
            it.subType == IoTAutomationType.TYPE_NOTIFICATION
        })
    }

    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter

    private lateinit var automationTypeAdapter: AutomationTypeAdapter

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_smart_stair_switch)
            spinnerFirstHour.adapter = hourFirstAdapter
            spinnerFirstMinute.adapter = minuteFirstAdapter
            spinnerSecondHour.adapter = hourSecondAdapter
            spinnerSecondMinute.adapter = minuteSecondAdapter
            spinnerTime.adapter = timeAdapter
            spinnerSmart.adapter = smartSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * After selecting a Smart Automation, get the Smart Notification of the Smart Automation
            * Show the device used for the Smart Notifcation
            * Show minimum time between notifications
            * Show start time and end time
            * */
            spinnerSmart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val ioTSmart = (it.getItemAtPosition(p2) as IoTSmart)
                        edtSmartName.setText(ioTSmart.label)
                        if (SmartSdk.smartFeatureHandler()
                                .getSmartFeatureNotification(ioTSmart.uuid) != null
                        ) {
                            ioTSmartNotification = SmartSdk.smartFeatureHandler()
                                .getSmartFeatureNotification(ioTSmart.uuid)
                        } else {
                            ioTSmartNotification = IoTSmartNotification()
                        }

                        val deviceList = SmartSdk.deviceHandler().all.filter {
                            getSupportedDevice().contains(it.devType)
                        }
                        deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                        spinnerDevice.adapter = deviceSpinnerAdapter
                        ioTSmartNotification.devId?.let {
                            if (deviceList.contains(
                                    SmartSdk.deviceHandler().get(ioTSmartNotification.devId)
                                )
                            ) {
                                spinnerDevice.setSelection(
                                    deviceList.indexOf(
                                        SmartSdk.deviceHandler().get(
                                            ioTSmartNotification.devId
                                        )
                                    )
                                )
                            }
                        }
                        ioTSmartNotification.minTime.let {
                            spinnerTime.setSelection(
                                it
                            )
                        }
                        ioTSmartNotification.timeJob?.let {
                            spinnerFirstHour.setSelection(
                                it[0] / 60
                            )
                            spinnerFirstMinute.setSelection(
                                it[0] % 60
                            )
                            spinnerSecondHour.setSelection(
                                it[1] / 60
                            )
                            spinnerSecondMinute.setSelection(
                                it[1] % 60
                            )
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * After selecting a device, show list of automation that supports the device.
            * Get the automation used for Smart Notifcation
            * */
            spinnerDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        automationTypeAdapter = AutomationTypeAdapter(
                            requireContext(),
                            AutomationType.getAutomationTypeList().filter { type ->
                                (it.getItemAtPosition(p2) as IoTDevice).containtFeature(type.automationAttr)
                            })
                        spinnerState.adapter = automationTypeAdapter
                        ioTSmartNotification.value?.let {
                            spinnerState.post(object : Runnable {
                                override fun run() {
                                    if (it.size != 1) {
                                        spinnerState.setSelection(
                                            AutomationType.getAutoTypePos(
                                                it[0],
                                                it[1]
                                            )
                                        )
                                    } else {
                                        spinnerState.setSelection(
                                            AutomationType.getAutoTypePos(it[0])
                                        )
                                    }
                                }
                            })
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
            btnEditSmart.setOnClickListener {
                /*
                * Bind Smart Notification to Smart Automation
                * */
                dialogLoading.show()
                ioTSmartNotification.devId =
                    (spinnerDevice.selectedItem as IoTDevice).uuid
                ioTSmartNotification.smartId = (spinnerSmart.selectedItem as IoTSmart).uuid
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
                            showNoti(R.string.update_success)
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
        when ((binding.spinnerState.selectedItem as AutomationType)) {
            AutomationType.ON_OFF, AutomationType.OPEN_CLOSE,
            AutomationType.ALL_PRESS_BUTTON, AutomationType.LOCK_UNLOCK,
            AutomationType.MOUNT_UNMOUNT, AutomationType.PRESENCE_UNPRESENCE -> return IoTCondition.ANY

            AutomationType.SINGLE_PRESS_BUTTON, AutomationType.DOUBLE_PRESS_BUTTON, AutomationType.LONG_PRESS_BUTTON -> return IoTCondition.EQUAL
            else -> {
                return IoTCondition.EQUAL
            }
        }

        when ((binding.spinnerDevice.selectedItem as IoTDevice).devType) {
            IoTDeviceType.DOOR_SENSOR,
            IoTDeviceType.PLUG,
            IoTDeviceType.AC,
            IoTDeviceType.MOTION_SENSOR,
            IoTDeviceType.DOORLOCK,
            IoTDeviceType.SWITCH -> return IoTCondition.EQUAL

            IoTDeviceType.MOTION_LUX_SENSOR -> return IoTCondition.EQUAL
            IoTDeviceType.PRESENSCE_SENSOR -> return IoTCondition.EQUAL
            else -> {
                return IoTCondition.EQUAL
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