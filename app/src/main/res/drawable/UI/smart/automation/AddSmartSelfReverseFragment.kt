package com.example.rogosample.UI.smart.automation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.AutomationTypeAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartSelfReverseBinding
import com.example.rogosample.`object`.AutomationType
import com.example.rogosample.`object`.ELement
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTCondition
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTReverseOnOff
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart

class AddSmartSelfReverseFragment : BaseFragment<FragmentAddSmartSelfReverseBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_self_reverse

    private var condition = -1
    private val elmList = arrayListOf<ELement>()
    private var ioTReverseOnOff: IoTReverseOnOff?= null
    private val hourFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 24).toList())
    }
    private val minuteFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val hourSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 24).toList())
    }
    private val minuteSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val deviceSpinnerAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), SmartSdk.deviceHandler().all.toList())
    }

    private val waitingMinuteAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val waitingSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }

    private val stateSpinnerAdapter by lazy {
        AutomationTypeAdapter(requireContext(), listOf<AutomationType>(
            AutomationType.ON,
            AutomationType.OFF
        ))
    }

    private val elementCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            notifyChange(it)
        })
    }

    /*
    * get the element of device
    * */
    private fun notifyChange(change: Pair<Int, Boolean>) {
        if (change.second) {
            ioTReverseOnOff?.elm = change.first
            elmList.forEach {elm ->
                elm.isChecked = elm.elmIndex == change.first
            }
        }
        binding.rvElement.post(object : Runnable {
            override fun run() {
                elementCheckAdapter.notifyDataSetChanged()
            }
        })
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_reverse_on_off)
            rvElement.adapter = elementCheckAdapter
            spinnerDevice.adapter = deviceSpinnerAdapter
            spinnerState.adapter = stateSpinnerAdapter
            spinnerFirstHour.adapter = hourFirstAdapter
            spinnerFirstMinute.adapter = minuteFirstAdapter
            spinnerSecondHour.adapter = hourSecondAdapter
            spinnerSecondMinute.adapter = minuteSecondAdapter
            spinnerWaitingMinute.adapter = waitingMinuteAdapter
            spinnerWaitingSecond.adapter = waitingSecondAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            ioTReverseOnOff = IoTReverseOnOff()
            /*
            * To get elements of device, check whether user updated element's label or not. If not show the index of the element
            * */
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTReverseOnOff?.devId = ioTDevice.uuid
                        if (ioTDevice.elementIds.isNotEmpty()) {
                            ioTDevice.elementInfos.forEach { entry ->
                                if (entry.value.label.isNullOrEmpty()) {
                                    elmList.add(
                                        ELement(
                                            entry.key,
                                            "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                        )
                                    )
                                } else {
                                    elmList.add(
                                        ELement(entry.key, entry.value.label)
                                    )
                                }
                            }
                            ILogR.D("elementSize", ioTDevice.elementInfos.size)
                            elementCheckAdapter.submitList(elmList)
                            elementCheckAdapter.notifyDataSetChanged()
                        } else {
                            ILogR.D("getElement", "No element")
                        }
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
                    IoTAutomationType.TYPE_SMART_REVERSE_ON_OFF,
                    intArrayOf(),
                    null,
                    object : RequestCallback<IoTSmart> {
                        override fun onSuccess(p0: IoTSmart?) {
                            /*
                            * Bind Smart Reverse On Off to Smart Automation
                            * */
                            ioTReverseOnOff?.smartId = p0?.uuid
                            ioTReverseOnOff?.devId = (spinnerDevice.selectedItem as IoTDevice).uuid
                            ioTReverseOnOff?.condition = getConditionByType()
                            if((spinnerState.selectedItem as AutomationType).automationType != null) {
                                ioTReverseOnOff?.value = intArrayOf((spinnerState.selectedItem as AutomationType).automationAttr,
                                    (spinnerState.selectedItem as AutomationType).automationType!!)
                            } else {
                                ioTReverseOnOff?.value = intArrayOf((spinnerState.selectedItem as AutomationType).automationAttr)
                            }
                            ioTReverseOnOff?.waitingTime = (spinnerWaitingMinute.selectedItem as Int) * 60 + (spinnerWaitingSecond.selectedItem as Int)
                            ioTReverseOnOff?.timeJob = intArrayOf(
                                ((spinnerFirstHour.selectedItem as Int) * 60 + (spinnerFirstMinute.selectedItem as Int)),
                                ((spinnerSecondHour.selectedItem as Int) * 60 + (spinnerSecondMinute.selectedItem as Int))
                            )
                            SmartSdk.smartFeatureHandler().bindSmartFeatureReverseOnOff(
                                ioTReverseOnOff,
                                object : RequestCallback<IoTReverseOnOff> {
                                    override fun onSuccess(p0: IoTReverseOnOff?) {
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
}