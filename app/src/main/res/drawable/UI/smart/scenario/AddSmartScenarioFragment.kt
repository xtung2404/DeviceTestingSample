package com.example.rogosample.UI.smart.scenario

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementSelectionAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartScenarioBinding
import com.example.rogosample.`object`.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd

class AddSmartScenarioFragment : BaseFragment<FragmentAddSmartScenarioBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_scenario

    private var deviceList = listOf<IoTDevice>()
    private val cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private val elementMap = hashMapOf<Int, String>()
    private var ioTTargetCmd: IoTTargetCmd? = null

    private val commandSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }
    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter

    /*
    * To add or remove a element from SmartCmd
    * */
    private val elementSelectionAdapter by lazy {
        ElementSelectionAdapter(isChecked = {
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

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_scenario)
            spinnerCommand.adapter = commandSpinnerAdapter
            rvElement.adapter = elementSelectionAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            /*
            * After selecting command, get devices support that command
            *
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
                        deviceList = SmartSdk.deviceHandler().all.filter { device ->
                            device.containtFeature(ioTCommand.cmdAttribute)
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
            /*
            * To get elements of device, check whether user updated element's label or not. If not show the index of the element
            * */
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elementMap.clear()
                        cmdsMap.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        ioTDevice.elementInfos.forEach { entry ->
                            if (entry.value.label.isNullOrEmpty()) {
                                elementMap[entry.key] =
                                    "Nút ${ioTDevice.getIndexElement(entry.key)}"
                            } else {
                                elementMap[entry.key] = entry.value.label
                            }
                        }
                        elementSelectionAdapter.submitList(elementMap.entries.toList())

                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * To add a smart scenario, create a smart scene
            * */
            btnAddScenario.setOnClickListener {
                SmartSdk.smartFeatureHandler().createSmartScene(
                    edtSmartName.text.toString(),
                    0,
                    null,
                    object : RequestCallback<IoTSmart> {
                        override fun onSuccess(p0: IoTSmart?) {
                            p0?.let {
                                /*
                                * Bind SmartCmd to Smart Scenario
                                * */
                                SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                                    it.uuid,
                                    (spinnerDevice.selectedItem as IoTDevice).uuid,
                                    cmdsMap,
                                    object : RequestCallback<IoTSmartCmd> {
                                        override fun onSuccess(p0: IoTSmartCmd?) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                showNoti(R.string.add_success)
                                            }
                                        }

                                        override fun onFailure(p0: Int, p1: String?) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                p1?.let { msg ->
                                                    showNoti(msg)
                                                }
                                            }
                                        }

                                    }
                                )

                            }

                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            p1?.let {
                                showNoti(it)
                            }
                        }

                    }
                )
            }
        }
    }
}