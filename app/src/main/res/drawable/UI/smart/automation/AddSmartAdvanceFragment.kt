package com.example.rogosample.UI.smart.automation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.AutomationTypeAdapter
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartAdvanceBinding
import com.example.rogosample.`object`.AutomationType
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.ELement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTCondition
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.platform.define.IoTTriggerTimeCfg
import rogo.iot.module.platform.define.IoTTriggerType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartTrigger
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd

class AddSmartAdvanceFragment : BaseFragment<FragmentAddSmartAdvanceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_advance

    private var condition = -1
    private var ioTSmartTrigger: IoTSmartTrigger? = null
    private val elmList = arrayListOf<ELement>()
    private var elmExtList = arrayListOf<ELement>()
    private var smartUUID: String? = null
    private val cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private var ioTTargetCmd: IoTTargetCmd? = null
    private var automationValue = intArrayOf()
    private var deviceList = listOf<IoTDevice>()
    private var deviceExtList = listOf<IoTDevice>()
    private val hourFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..23).toList())
    }
    private val minuteFirstAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }
    private val hourSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..23).toList())
    }
    private val minuteSecondAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..59).toList())
    }

    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter
    private lateinit var deviceExtSpinnerAdapter: DeviceSpinnerAdapter

    private val waitingMinuteAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..30).toList())
    }

    private lateinit var commandSpinnerAdapter: AutomationTypeAdapter

    private val commandExtSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }
    private val elementCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            notifyChange(it)
        })
    }

    private val elementExtCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            notifyExtChange(it)
        })
    }
    /*
    * Add the elmOwn
    * */
    private fun notifyChange(change: Pair<Int, Boolean>) {
        if (change.second) {
            ioTSmartTrigger?.elm = change.first
            elementExtCheckAdapter.submitList(elmExtList.filter {
                it.elmIndex != ioTSmartTrigger?.elm
            })
            elmList.forEach { elm ->
                elm.isChecked = elm.elmIndex == change.first
            }
        }
        binding.rvElement.post(object : Runnable {
            override fun run() {
                elementCheckAdapter.notifyDataSetChanged()
            }
        })
    }

    /*
    * Add the elmExt
    * */
    private fun notifyExtChange(change: Pair<Int, Boolean>) {
        when (binding.spinnerCommandExt.selectedItem as Command) {
            Command.ON -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.ON.cmdAttribute, Command.ON.cmdType)
                )
            }

            Command.OFF -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.OFF.cmdAttribute, Command.OFF.cmdType)
                )
            }

            Command.SYNCON -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.SYNCON.cmdAttribute, 0)
                )
            }

            Command.SYNCOFF -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.SYNCOFF.cmdAttribute, 0)
                )
            }

            Command.OPEN -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.OPEN.cmdAttribute, Command.OPEN.cmdType)
                )
            }

            Command.CLOSE -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(Command.CLOSE.cmdAttribute, Command.CLOSE.cmdType)
                )
            }

            Command.BRIGHTNESS -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(
                        Command.BRIGHTNESS.cmdAttribute,
                        binding.sbBrightness.progress,
                        binding.sbKelvin.progress
                    )
                )
            }

            Command.COLOR -> {
                ioTTargetCmd = IoTTargetCmd(
                    0,
                    intArrayOf(
                        (binding.sbHue.progress * 0.2F).toInt(),
                        binding.sbSaturation.progress.toInt(),
                        1
                    )
                )
            }

            else -> {

            }
        }
        if (cmdsMap.keys.contains(change.first)) {
            if (!change.second) {
                cmdsMap.remove(change.first)
            }
        } else {
            if (change.second) {
                cmdsMap[change.first] = ioTTargetCmd!!
            }
        }
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_advance)
            rvElement.adapter = elementCheckAdapter
            commandSpinnerAdapter = AutomationTypeAdapter(requireContext(), AutomationType.getAutomationTypeList())
            spinnerCommand.adapter = commandSpinnerAdapter
            spinnerFirstHour.adapter = hourFirstAdapter
            spinnerFirstMinute.adapter = minuteFirstAdapter
            spinnerSecondHour.adapter = hourSecondAdapter
            spinnerSecondMinute.adapter = minuteSecondAdapter
            spinnerTime.adapter = waitingMinuteAdapter
            spinnerCommandExt.adapter = commandExtSpinnerAdapter
            rvElementExt.adapter = elementExtCheckAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            ioTSmartTrigger = IoTSmartTrigger()
            /*
            * After selecting a device, show list of commands that supports the device
            * */
            spinnerDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTSmartTrigger?.devId = ioTDevice.uuid
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
                        elementCheckAdapter.submitList(elmList)
                        elementCheckAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * After selecting a command, show list of devices that supports the command
            * */
            spinnerCommand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val typeSelected = it.getItemAtPosition(p2) as AutomationType
                        if(typeSelected.automationType != null) {
                            automationValue = intArrayOf(typeSelected.automationAttr, typeSelected.automationType)
                        } else {
                            automationValue = intArrayOf(typeSelected.automationAttr)
                        }
                        deviceList = SmartSdk.deviceHandler().all.filter { device ->
                            device.containtFeature(typeSelected.automationAttr)
                        }
                        deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                        spinnerDevice.adapter = deviceSpinnerAdapter
                        if (deviceList.isNullOrEmpty()) {
                            lnSelectDevice.visibility = View.GONE
                        } else {
                            lnSelectDevice.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            spinnerCommandExt.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val ioTCommand = it.getItemAtPosition(p2) as Command
                        when (ioTCommand) {
                            Command.BRIGHTNESS -> {
                                lnBrightnessExt.visibility = View.VISIBLE
                                lnSaturationExt.visibility = View.GONE
                            }

                            Command.COLOR -> {
                                lnSaturationExt.visibility = View.VISIBLE
                                lnBrightnessExt.visibility = View.GONE
                            }

                            else -> {
                                lnBrightnessExt.visibility = View.GONE
                                lnSaturationExt.visibility = View.GONE
                            }
                        }
                        deviceExtList = SmartSdk.deviceHandler().all.filter { device ->
                            if (ioTCommand == Command.SYNCON || ioTCommand == Command.SYNCOFF) {
                                device.containtFeature(IoTAttribute.ACT_ONOFF)
                            } else {
                                device.containtFeature(ioTCommand.cmdAttribute)
                            }
                        }
                        deviceExtSpinnerAdapter =
                            DeviceSpinnerAdapter(requireContext(), deviceExtList)
                        spinnerDeviceExt.adapter = deviceExtSpinnerAdapter
                        if (deviceExtList.isNullOrEmpty()) {
                            lnSelectDeviceExt.visibility = View.GONE
                        } else {
                            lnSelectDeviceExt.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

            spinnerDeviceExt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmExtList.clear()
                        cmdsMap.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTDevice.elementInfos.forEach { entry ->
                            if (entry.value.label.isNullOrEmpty()) {
                                elmExtList.add(
                                    ELement(
                                        entry.key,
                                        "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                    )
                                )
                            } else {
                                elmExtList.add(
                                    ELement(entry.key, entry.value.label)
                                )
                            }
                        }
                        elementExtCheckAdapter.submitList(elmExtList)
                        elementExtCheckAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            btnContinue.setOnClickListener {
                if (edtSmartName.text.isNullOrEmpty()) {
                    showNoti(R.string.check_your_information)
                } else {
                    addSmart()
                }
            }
            btnAddSmart.setOnClickListener {
                addSmartCmd()
            }
        }
    }

    fun addSmart() {
        binding.apply {
            /*
            * Create a Smart Automation
            */
            dialogLoading.show()
            SmartSdk.smartFeatureHandler().createSmartAutomation(
                edtSmartName.text.toString(),
                IoTAutomationType.TYPE_MIX_OR,
                intArrayOf(),
                null,
                object : RequestCallback<IoTSmart> {
                    override fun onSuccess(p0: IoTSmart?) {
                        /*
                        * Bind Smart Trigger to Smart Automation
                        * */
                        smartUUID = p0?.uuid
                        ioTSmartTrigger?.smartId = p0?.uuid
                        ioTSmartTrigger?.devId = (spinnerDevice.selectedItem as IoTDevice).uuid
                        ioTSmartTrigger?.type = IoTTriggerType.OWNER
                        ioTSmartTrigger?.value = automationValue
                        ioTSmartTrigger?.condition = getConditionByType()
                        ioTSmartTrigger?.timeCfg = intArrayOf(
                            IoTTriggerTimeCfg.MIN_TIME,
                            (spinnerTime.selectedItem as Int)
                        )
                        ioTSmartTrigger?.zone = 0
                        val startTime =
                            (spinnerFirstHour.selectedItem as Int) * 60 + (spinnerFirstMinute.selectedItem as Int)
                        val endTime =
                            (spinnerSecondHour.selectedItem as Int) * 60 + (spinnerSecondMinute.selectedItem as Int)
                        if (startTime != 0 && endTime != 0) {
                            ioTSmartTrigger?.timeJob = intArrayOf(
                                startTime, endTime
                            )
                        }
                        SmartSdk.smartFeatureHandler().bindSmartTrigger(
                            ioTSmartTrigger,
                            object : RequestCallback<IoTSmartTrigger> {
                                override fun onSuccess(p0: IoTSmartTrigger?) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        dialogLoading.dismiss()
                                        lnDevOwn.visibility = View.GONE
                                        lnDevExt.visibility = View.VISIBLE
                                    }
                                }

                                override fun onFailure(p0: Int, p1: String?) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        dialogLoading.dismiss()
                                        p1?.let {
                                            showNoti(it)
                                        }
                                    }
                                }

                            }
                        )
                    }

                    override fun onFailure(p0: Int, p1: String?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogLoading.dismiss()
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    }

                }
            )
        }
    }

    fun addSmartCmd() {
        smartUUID?.let {
            SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                it,
                (binding.spinnerDeviceExt.selectedItem as IoTDevice).uuid,
                cmdsMap,
                object : RequestCallback<IoTSmartCmd> {
                    override fun onSuccess(p0: IoTSmartCmd?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogLoading.dismiss()
                            SmartSdk.smartFeatureHandler().activeSmart(it)
                            showNoti(R.string.add_success)
                        }
                    }

                    override fun onFailure(p0: Int, p1: String?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogLoading.dismiss()
                            p1?.let {
                                showNoti(it)
                            }
                        }
                    }

                }
            )
        }
    }

    fun getConditionByType(): Int {
        if (condition == -1) {
            // actions list size =1
            val state = (binding.spinnerCommand.selectedItem as AutomationType)
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