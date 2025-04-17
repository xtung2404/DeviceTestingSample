package com.example.rogosample.UI.smart.automation.editAutomation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditSmartAdvanceBinding
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.ELement
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTAutomationType
import rogo.iot.module.platform.define.IoTCondition
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.platform.define.IoTTriggerTimeCfg
import rogo.iot.module.platform.define.IoTTriggerType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartTrigger
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd

class EditSmartAdvanceFragment : BaseFragment<FragmentEditSmartAdvanceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_smart_advance

    private lateinit var ioTSmartTrigger: IoTSmartTrigger
    private val elmList = arrayListOf<ELement>()
    private var smartUUID: String? = null
    private var deviceUuid: String? = null
    private var cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private var ioTSmart: IoTSmart? = null
    private var ioTTargetCmd: IoTTargetCmd? = null
    private var elmExtList = arrayListOf<ELement>()
    private var cmdValue = intArrayOf()
    private var deviceList = listOf<IoTDevice>()
    private var deviceExtList = listOf<IoTDevice>()
    private val smartSpinnerAdapter by lazy {
        SmartSpinnerAdapter(
            requireContext(),
            SmartSdk.smartFeatureHandler().getSmartByType(IoTSmartType.TYPE_AUTOMATION)
                .filter {
                    it.subType == IoTAutomationType.TYPE_MIX_AND || it.subType == IoTAutomationType.TYPE_MIX_OR
                }.toList()
        )
    }
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

    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter
    private lateinit var deviceExtSpinnerAdapter: DeviceSpinnerAdapter

    private val waitingMinuteAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..30).toList())
    }

    private val commandSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }

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

    private fun notifyChange(change: Pair<Int, Boolean>) {
        if (change.second) {
            ioTSmartTrigger.elm = change.first
            elementExtCheckAdapter.submitList(elmExtList.filter {
                it.elmIndex != ioTSmartTrigger.elm
            })
            when (binding.spinnerCommand.selectedItem as Command) {
                Command.ON -> {
                    cmdValue = intArrayOf(Command.ON.cmdAttribute, Command.ON.cmdType)
                }

                Command.OFF -> {
                    cmdValue = intArrayOf(Command.OFF.cmdAttribute, Command.OFF.cmdType)
                }

                Command.OPEN -> {
                    cmdValue = intArrayOf(Command.OPEN.cmdAttribute, Command.OPEN.cmdType)
                }

                Command.CLOSE -> {
                    cmdValue = intArrayOf(Command.CLOSE.cmdAttribute, Command.CLOSE.cmdType)
                }

                Command.BRIGHTNESS -> {
                    cmdValue = intArrayOf(
                        Command.BRIGHTNESS.cmdAttribute,
                        binding.sbBrightness.progress,
                        binding.sbKelvin.progress
                    )
                }

                Command.COLOR -> {
                    cmdValue = intArrayOf(
                        (binding.sbHue.progress * 0.2F).toInt(),
                        binding.sbSaturation.progress.toInt(),
                        1
                    )
                }

                else -> {

                }
            }
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
            toolbar.txtTitle.text = resources.getString(R.string.edit_smart_advance)
            rvElement.adapter = elementCheckAdapter
            spinnerCommand.adapter = commandSpinnerAdapter
            spinnerFirstHour.adapter = hourFirstAdapter
            spinnerFirstMinute.adapter = minuteFirstAdapter
            spinnerSecondHour.adapter = hourSecondAdapter
            spinnerSecondMinute.adapter = minuteSecondAdapter
            spinnerTime.adapter = waitingMinuteAdapter
            rvElementExt.adapter = elementExtCheckAdapter
            spinnerSmart.adapter = smartSpinnerAdapter
            spinnerCommandExt.adapter = commandExtSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * After selecting a command, get list of devices that supports the command.
            * Show the device used for Smart Trigger
            * */
            spinnerCommand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val ioTCommand = it.getItemAtPosition(p2) as Command
                        when (ioTCommand) {
                            Command.BRIGHTNESS -> {
                                lnBrightness.visibility = View.VISIBLE
                                lnSaturation.visibility = View.GONE
                            }

                            Command.COLOR -> {
                                lnSaturation.visibility = View.VISIBLE
                                lnBrightness.visibility = View.GONE
                            }

                            else -> {
                                lnBrightness.visibility = View.GONE
                                lnSaturation.visibility = View.GONE
                            }
                        }
                        spinnerDevice.post(object : Runnable {
                            override fun run() {
                                deviceList = SmartSdk.deviceHandler().all.filter {
                                    it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                }
                                deviceSpinnerAdapter =
                                    DeviceSpinnerAdapter(requireContext(), deviceList)
                                spinnerDevice.adapter = deviceSpinnerAdapter
                                ioTSmartTrigger.devId?.let {
                                    spinnerDevice.setSelection(
                                        deviceList.indexOf(
                                            SmartSdk.deviceHandler().get(ioTSmartTrigger.devId)
                                        ), true
                                    )
                                }
                                deviceSpinnerAdapter.notifyDataSetChanged()
                            }
                        })
                        if (deviceList.isNotEmpty()) {
                            lnSelectDevice.visibility = View.VISIBLE
                        } else {
                            lnSelectDevice.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * After selecting a smart -> get the Smart Trigger.
            * Show the time config, start time, end time
            * Show the command used for Smart Trigger.
            * Show the device used for Smart Trigger
            * */
            spinnerSmart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        ioTSmart = it.getItemAtPosition(p2) as IoTSmart
                        smartUUID = ioTSmart?.uuid
                        if (SmartSdk.smartFeatureHandler().getSmartTriggers(ioTSmart?.uuid)
                                .isNullOrEmpty()
                        ) {
                            lnEditCommand.visibility = View.GONE
                            ioTSmartTrigger = IoTSmartTrigger()
                            edtSmartName.setText(ioTSmart?.label)
                        } else {
                            lnEditCommand.visibility = View.VISIBLE
                            edtSmartName.setText(ioTSmart?.label)
                            ioTSmartTrigger =
                                SmartSdk.smartFeatureHandler().getSmartTriggers(ioTSmart?.uuid)
                                    .first()
                            cmdValue = ioTSmartTrigger.value
                            if (ioTSmartTrigger.timeCfg[0] == IoTTriggerTimeCfg.WAITING_TIME) {
                                spinnerTime.setSelection(ioTSmartTrigger.timeCfg[1])
                            }
                            ioTSmartTrigger.timeJob?.let {
                                spinnerFirstHour.setSelection(
                                    ioTSmartTrigger.timeJob[0] / 60
                                )
                                spinnerFirstMinute.setSelection(
                                    ioTSmartTrigger.timeJob[0] % 60
                                )
                                spinnerSecondHour.setSelection(
                                    ioTSmartTrigger.timeJob[1] / 60
                                )
                                spinnerSecondMinute.setSelection(
                                    ioTSmartTrigger.timeJob[1] % 60
                                )
                            }
                            when (ioTSmartTrigger.value.toList().first()) {
                                IoTAttribute.ACT_BRIGHTNESS_KELVIN -> {
                                    lnBrightness.visibility = View.VISIBLE
                                    lnSaturation.visibility = View.GONE
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTSmartTrigger.value.toList()[0]
                                                ), true
                                            )
                                        }

                                    })
                                    sbBrightness.progress =
                                        ioTSmartTrigger.value.toList()[1]
                                    sbKelvin.progress = ioTSmartTrigger.value.toList()[2]
                                }

                                IoTAttribute.ACT_COLOR_HSV -> {
                                    lnBrightness.visibility = View.GONE
                                    lnSaturation.visibility = View.VISIBLE
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTSmartTrigger.value.toList()[0]
                                                ), true
                                            )
                                        }
                                    })
                                    sbHue.progress = ioTSmartTrigger.value.toList()[1]
                                    sbSaturation.progress =
                                        ioTSmartTrigger.value.toList()[2]
                                }

                                else -> {
                                    lnBrightness.visibility = View.GONE
                                    lnSaturation.visibility = View.GONE
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTSmartTrigger.value.toList()[0],
                                                    ioTSmartTrigger.value.toList()[1]
                                                ), true
                                            )
                                        }

                                    })

                                }
                            }
                            spinnerDevice.post(object : Runnable {
                                override fun run() {
                                    deviceList = SmartSdk.deviceHandler().all.filter {
                                        it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                    }
                                    deviceSpinnerAdapter =
                                        DeviceSpinnerAdapter(requireContext(), deviceList)
                                    spinnerDevice.adapter = deviceSpinnerAdapter
                                    spinnerDevice.setSelection(
                                        deviceList.indexOf(
                                            SmartSdk.deviceHandler().get(ioTSmartTrigger.devId)
                                        ), true
                                    )
                                    deviceSpinnerAdapter.notifyDataSetChanged()
                                }
                            })
                            if (deviceList.isNotEmpty()) {
                                lnSelectDevice.visibility = View.VISIBLE
                            } else {
                                lnSelectDevice.visibility = View.GONE
                            }
                        }
                        if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                .isNotEmpty()
                        ) {
                            val ioTSmartCmd =
                                SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid).first()
                            ioTSmartCmd?.cmds?.let {
                                cmdsMap = it
                                deviceUuid = ioTSmartCmd.targetId
                                ioTTargetCmd = it.entries.first().value
                                /*
                                * Show the device used for SmartCmd
                                * */
                                spinnerDeviceExt.setSelection(
                                    deviceExtList.indexOf(
                                        SmartSdk.deviceHandler().get(ioTSmartCmd.targetId)
                                    )
                                )

                                /*
                                * Show the command and the value of the command of SmartScenario
                                * */
                                when (ioTTargetCmd?.cmd?.toList()?.first()) {
                                    IoTAttribute.ACT_BRIGHTNESS_KELVIN -> {
                                        spinnerCommandExt.post(object : Runnable {
                                            override fun run() {
                                                spinnerCommandExt.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0)
                                                    ), true
                                                )
                                            }

                                        })
                                        sbBrightnessExt.progress =
                                            ioTTargetCmd?.cmd?.toList()?.get(1)!!
                                        sbKelvinExt.progress = ioTTargetCmd?.cmd?.toList()?.get(2)!!
                                    }

                                    IoTAttribute.ACT_COLOR_HSV -> {
                                        lnBrightnessExt.visibility = View.GONE
                                        lnSaturationExt.visibility = View.VISIBLE
                                        spinnerCommandExt.post(object : Runnable {
                                            override fun run() {
                                                spinnerCommandExt.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0)
                                                    ), true
                                                )
                                            }

                                        })
                                        sbHueExt.progress = ioTTargetCmd?.cmd?.toList()?.get(1)!!
                                        sbSaturationExt.progress =
                                            ioTTargetCmd?.cmd?.toList()?.get(2)!!
                                    }

                                    else -> {
                                        lnBrightnessExt.visibility = View.GONE
                                        lnSaturationExt.visibility = View.GONE
                                        spinnerCommandExt.post(object : Runnable {
                                            override fun run() {
                                                spinnerCommandExt.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0),
                                                        ioTTargetCmd?.cmd?.toList()?.get(1)
                                                    ), true
                                                )
                                            }
                                        })
                                    }
                                }
                                spinnerDeviceExt.post(object : Runnable {
                                    override fun run() {
                                        deviceExtList = SmartSdk.deviceHandler().all.filter {
                                            it.containtFeature((spinnerCommandExt.selectedItem as Command).cmdAttribute)
                                        }
                                        deviceExtSpinnerAdapter =
                                            DeviceSpinnerAdapter(requireContext(), deviceExtList)
                                        spinnerDeviceExt.adapter = deviceExtSpinnerAdapter
                                        spinnerDeviceExt.setSelection(
                                            deviceExtList.indexOf(
                                                SmartSdk.deviceHandler().get(deviceUuid)
                                            ), true
                                        )
                                        deviceExtSpinnerAdapter.notifyDataSetChanged()
                                    }
                                })
                                if (deviceExtList.isNotEmpty()) {
                                    lnSelectDeviceExt.visibility = View.VISIBLE
                                } else {
                                    lnSelectDeviceExt.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * To get elements of device, check whether user updated element's label or not. If not show the index of the element.
            * Get available list of elements for elmExt
            * */
            spinnerDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTDevice.elementInfos.forEach { entry ->
                            if (entry.value.label.isNullOrEmpty()) {
                                elmList.add(
                                    ELement(
                                        entry.key,
                                        "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                    )
                                )
                            } else {
                                elmList.add(ELement(entry.key, entry.value.label))
                            }
                        }
                        for (elm in elmList) {
                            elm.isChecked = ioTSmartTrigger.elm == elm.elmIndex
                        }
                        elementCheckAdapter.submitList(elmList)
                        elementCheckAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            spinnerCommandExt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                            device.containtFeature(ioTCommand.cmdAttribute)
                        }
                        if (cmdsMap.isNotEmpty()) {
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

                                Command.OPEN -> {
                                    ioTTargetCmd = IoTTargetCmd(
                                        0,
                                        intArrayOf(Command.OPEN.cmdAttribute, Command.OPEN.cmdType)
                                    )
                                }

                                Command.CLOSE -> {
                                    ioTTargetCmd = IoTTargetCmd(
                                        0,
                                        intArrayOf(
                                            Command.CLOSE.cmdAttribute,
                                            Command.CLOSE.cmdType
                                        )
                                    )
                                }

                                Command.BRIGHTNESS -> {
                                    ioTTargetCmd = IoTTargetCmd(
                                        0,
                                        intArrayOf(
                                            Command.BRIGHTNESS.cmdAttribute,
                                            binding.sbBrightnessExt.progress,
                                            binding.sbKelvinExt.progress
                                        )
                                    )
                                }

                                Command.COLOR -> {
                                    ioTTargetCmd = IoTTargetCmd(
                                        0,
                                        intArrayOf(
                                            (binding.sbHueExt.progress * 0.2F).toInt(),
                                            binding.sbSaturationExt.progress.toInt(),
                                            1
                                        )
                                    )
                                }

                                else -> {

                                }
                            }
                            for (entry in cmdsMap.entries) {
                                cmdsMap[entry.key] = ioTTargetCmd!!
                            }
                        }
                        if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                .isNotEmpty()
                        ) {
                            spinnerDeviceExt.post(object : Runnable {
                                override fun run() {
                                    deviceExtList = SmartSdk.deviceHandler().all.filter {
                                        it.containtFeature((spinnerCommandExt.selectedItem as Command).cmdAttribute)
                                    }
                                    deviceExtSpinnerAdapter =
                                        DeviceSpinnerAdapter(requireContext(), deviceExtList)
                                    spinnerDeviceExt.adapter = deviceExtSpinnerAdapter
                                    spinnerDeviceExt.setSelection(
                                        deviceExtList.indexOf(
                                            SmartSdk.deviceHandler().get(deviceUuid)
                                        ), true
                                    )
                                    deviceExtSpinnerAdapter.notifyDataSetChanged()
                                }
                            })
                        }
                        if (deviceExtList.isNotEmpty()) {
                            lnSelectDeviceExt.visibility = View.VISIBLE
                        } else {
                            lnSelectDeviceExt.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

            spinnerDeviceExt.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elmExtList.clear()
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
                                elmExtList.add(ELement(entry.key, entry.value.label))
                            }
                        }
                        for (elm in elmExtList) {
                            elm.isChecked = cmdsMap.keys.contains(elm.elmIndex)
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
                    editSmart()
                }
            }

            btnEditSmart.setOnClickListener {
                edtSmartCmd()
            }
        }
    }

    private fun editSmart() {
        binding.apply {
            /*
            * Bind Smart Trigger to selected Smart Automation
            * */
            dialogLoading.show()
            ioTSmartTrigger.smartId = (spinnerSmart.selectedItem as IoTSmart).uuid
            ioTSmartTrigger.devId = (spinnerDevice.selectedItem as IoTDevice).uuid
            ioTSmartTrigger.type = IoTTriggerType.OWNER
            ioTSmartTrigger.value = cmdValue
            ioTSmartTrigger.condition = IoTCondition.ANY
            ioTSmartTrigger.timeCfg = intArrayOf(
                IoTTriggerTimeCfg.MIN_TIME,
                (spinnerTime.selectedItem as Int)
            )
            ioTSmartTrigger.zone = 0
            val startTime =
                (spinnerFirstHour.selectedItem as Int) * 60 + (spinnerFirstMinute.selectedItem as Int)
            val endTime =
                (spinnerSecondHour.selectedItem as Int) * 60 + (spinnerSecondMinute.selectedItem as Int)
            if (startTime != 0 && endTime != 0) {
                ioTSmartTrigger.timeJob = intArrayOf(
                    (spinnerFirstHour.selectedItem as Int) * 60 + (spinnerFirstMinute.selectedItem as Int),
                    (spinnerSecondHour.selectedItem as Int) * 60 + (spinnerSecondMinute.selectedItem as Int)
                )
            }
            SmartSdk.smartFeatureHandler().bindSmartTrigger(
                ioTSmartTrigger,
                object : RequestCallback<IoTSmartTrigger> {
                    override fun onSuccess(p0: IoTSmartTrigger?) {
                        dialogLoading.dismiss()
                        lnDevOwn.visibility = View.GONE
                        lnDevExt.visibility = View.VISIBLE

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

    fun edtSmartCmd() {
        dialogLoading.show()
        smartUUID?.let {
            SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                it,
                (binding.spinnerDeviceExt.selectedItem as IoTDevice).uuid,
                cmdsMap,
                object : RequestCallback<IoTSmartCmd> {
                    override fun onSuccess(p0: IoTSmartCmd?) {
                        dialogLoading.dismiss()
                        SmartSdk.smartFeatureHandler().activeSmart(it)
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
    }
}