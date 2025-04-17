package com.example.rogosample.UI.smart.scenario

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.ElementSelectionAdapter
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditSmartScenarioBinding
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.ELement
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartTrigger
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd

class EditSmartScenarioFragment : BaseFragment<FragmentEditSmartScenarioBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_smart_scenario
    private var deviceList = listOf<IoTDevice>()
    private val elementList = arrayListOf<ELement>()
    private var cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private var ioTSmart: IoTSmart? = null
    private var deviceUuid: String?= null
    private var ioTTargetCmd: IoTTargetCmd? = null
    /*
    * Get list of Smart Scenario
    * */
    private val smartSpinnerAdapter by lazy {
        SmartSpinnerAdapter(
            requireContext(),
            SmartSdk.smartFeatureHandler().getSmartByType(IoTSmartType.TYPE_SCENE).toList()
        )
    }
    private val commandSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }
    /*
    * To add or remove a element from SmartCmd
    * */
    private val elementCheckAdapter by lazy {
        ElementCheckAdapter(isChecked = {
            when (binding.spinnerCommand.selectedItem as Command) {
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
            if (cmdsMap.keys.contains(it.first)) {
                if (!it.second) {
                    cmdsMap.remove(it.first)
                }
            } else {
                if (it.second) {
                    cmdsMap[it.first] = ioTTargetCmd!!
                }
            }
        })
    }
    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.edit_smart_scenario)
            spinnerSmart.adapter = smartSpinnerAdapter
            spinnerCommand.adapter = commandSpinnerAdapter
            rvElement.adapter = elementCheckAdapter
            deviceList = SmartSdk.deviceHandler().all.filter {
                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
            }
            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
            spinnerDevice.adapter = deviceSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * Get smart scenario -> Get SmartCmd of Smart Scenario
            * */
            spinnerSmart.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        ioTSmart = (it.getItemAtPosition(p2) as IoTSmart)
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
                                spinnerDevice.setSelection(
                                    deviceList.indexOf(
                                        SmartSdk.deviceHandler().get(ioTSmartCmd.targetId)
                                    )
                                )

                                /*
                                * Show the command and the value of the command of SmartScenario
                                * */
                                when (ioTTargetCmd?.cmd?.toList()?.first()) {
                                    IoTAttribute.ACT_BRIGHTNESS_KELVIN -> {
                                        spinnerCommand.post(object: Runnable {
                                            override fun run() {
                                                spinnerCommand.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0)
                                                    ), true
                                                )
                                                if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                                        .isNotEmpty()
                                                ) {
                                                    spinnerDevice.post(object : Runnable {
                                                        override fun run() {
                                                            ILogR.D("tung",
                                                                (deviceList.indexOf(
                                                                    SmartSdk.deviceHandler().get(deviceUuid)
                                                                )))
                                                            deviceList = SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                                                            spinnerDevice.adapter = deviceSpinnerAdapter
                                                            spinnerDevice.setSelection(
                                                                deviceList.indexOf(
                                                                    SmartSdk.deviceHandler().get(deviceUuid)
                                                                ), true
                                                            )
                                                            deviceSpinnerAdapter.notifyDataSetChanged()
                                                        }
                                                    })
                                                }
                                                if(deviceList.isNotEmpty()) {
                                                    lnSelectDevice.visibility = View.VISIBLE
                                                } else {
                                                    lnSelectDevice.visibility = View.GONE
                                                }
                                            }

                                        })
                                        sbBrightness.progress =
                                            ioTTargetCmd?.cmd?.toList()?.get(1)!!
                                        sbKelvin.progress = ioTTargetCmd?.cmd?.toList()?.get(2)!!
                                    }

                                    IoTAttribute.ACT_COLOR_HSV -> {
                                        lnBrightness.visibility = View.GONE
                                        lnSaturation.visibility = View.VISIBLE
                                        spinnerCommand.post(object: Runnable {
                                            override fun run() {
                                                spinnerCommand.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0)
                                                    ), true
                                                )
                                                if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                                        .isNotEmpty()
                                                ) {
                                                    spinnerDevice.post(object : Runnable {
                                                        override fun run() {
                                                            ILogR.D("tung",
                                                                (deviceList.indexOf(
                                                                    SmartSdk.deviceHandler().get(deviceUuid)
                                                                )))
                                                            deviceList = SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                                                            spinnerDevice.adapter = deviceSpinnerAdapter
                                                            spinnerDevice.setSelection(
                                                                deviceList.indexOf(
                                                                    SmartSdk.deviceHandler().get(deviceUuid)
                                                                ), true
                                                            )
                                                            deviceSpinnerAdapter.notifyDataSetChanged()
                                                        }
                                                    })
                                                }
                                                if(deviceList.isNotEmpty()) {
                                                    lnSelectDevice.visibility = View.VISIBLE
                                                } else {
                                                    lnSelectDevice.visibility = View.GONE
                                                }
                                            }

                                        })
                                        sbHue.progress = ioTTargetCmd?.cmd?.toList()?.get(1)!!
                                        sbSaturation.progress =
                                            ioTTargetCmd?.cmd?.toList()?.get(2)!!
                                    }

                                    else -> {
                                        lnBrightness.visibility = View.GONE
                                        lnSaturation.visibility = View.GONE
                                        spinnerCommand.post(object: Runnable {
                                            override fun run() {
                                                spinnerCommand.setSelection(
                                                    Command.getCmdPos(
                                                        ioTTargetCmd?.cmd?.toList()?.get(0),
                                                        ioTTargetCmd?.cmd?.toList()?.get(1)
                                                    ), true
                                                )
                                                if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                                        .isNotEmpty()
                                                ) {
                                                    spinnerDevice.post(object : Runnable {
                                                        override fun run() {
                                                            deviceList = SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                            deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                                                            spinnerDevice.adapter = deviceSpinnerAdapter
                                                            spinnerDevice.setSelection(
                                                                deviceList.indexOf(
                                                                    SmartSdk.deviceHandler().get(deviceUuid)
                                                                ), true
                                                            )
                                                            deviceSpinnerAdapter.notifyDataSetChanged()
                                                        }
                                                    })
                                                }
                                                if(deviceList.isNotEmpty()) {
                                                    lnSelectDevice.visibility = View.VISIBLE
                                                } else {
                                                    lnSelectDevice.visibility = View.GONE
                                                }
                                            }
                                        })

                                    }
                                }
                            }
                        }
                        edtSmartName.setText((it.getItemAtPosition(p2) as IoTSmart).label)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
            /*
            * After selecting a device, show elements of device. Check which elements used for SmartCmd
            * */
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elementList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTDevice.elementInfos.forEach { entry ->
                            if (entry.value.label.isNullOrEmpty()) {
                                elementList.add(
                                    ELement(
                                        entry.key,
                                        "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                    )
                                )
                            } else {
                                elementList.add(ELement(entry.key, entry.value.label))
                            }
                        }
                        for (elm in elementList) {
                            elm.isChecked = cmdsMap.keys.contains(elm.elmIndex)
                        }
                        elementCheckAdapter.submitList(elementList)
                        elementCheckAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * After selecting a command, get devices support for the type of command.
            * */
            spinnerCommand.onItemSelectedListener = object : OnItemSelectedListener {
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
                        deviceList = SmartSdk.deviceHandler().all.filter { device ->
                            device.containtFeature(ioTCommand.cmdAttribute)
                        }
                        if (cmdsMap.isNotEmpty()) {
                            when (binding.spinnerCommand.selectedItem as Command) {
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
                            for (entry in cmdsMap.entries) {
                                cmdsMap[entry.key] = ioTTargetCmd!!
                            }
                        }
                        if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                .isNotEmpty()
                        ) {
                            spinnerDevice.post(object : Runnable {
                                override fun run() {
                                    deviceList = SmartSdk.deviceHandler().all.filter {
                                        it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                    }
                                    deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                                    spinnerDevice.adapter = deviceSpinnerAdapter
                                    spinnerDevice.setSelection(
                                        deviceList.indexOf(
                                            SmartSdk.deviceHandler().get(deviceUuid)
                                        ), true
                                    )
                                    deviceSpinnerAdapter.notifyDataSetChanged()
                                }
                            })
                        }
                        if(deviceList.isNotEmpty()) {
                            lnSelectDevice.visibility = View.VISIBLE
                        } else {
                            lnSelectDevice.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

            btnEditSmart.setOnClickListener {
                if (!edtSmartName.text.toString()
                        .contentEquals((spinnerSmart.selectedItem as IoTSmart).label)
                ) {
                    /*
                    * To update SmartScenario's label
                    * */
                    SmartSdk.smartFeatureHandler().setSmartLabel(
                        (spinnerSmart.selectedItem as IoTSmart).uuid,
                        edtSmartName.text.toString(),
                        object : RequestCallback<IoTSmart> {
                            override fun onSuccess(p0: IoTSmart?) {
                                updateSmartCmd()
                            }

                            override fun onFailure(p0: Int, p1: String?) {
                                p1?.let {
                                    showNoti(it)
                                }
                            }

                        }
                    )
                } else {
                    updateSmartCmd()
                }

            }
        }
    }

    private fun updateSmartCmd() {
        /*
        * Bind SmartCmd to SmartScenario
        * */
        binding.apply {
            SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                (spinnerSmart.selectedItem as IoTSmart).uuid,
                (spinnerDevice.selectedItem as IoTDevice).uuid,
                cmdsMap,
                object : RequestCallback<IoTSmartCmd> {
                    override fun onSuccess(p0: IoTSmartCmd?) {
                        showNoti(R.string.update_success)
                    }

                    override fun onFailure(p0: Int, p1: String?) {
                        p1?.let {
                            showNoti(p1)
                        }
                    }
                }
            )
        }
    }
}