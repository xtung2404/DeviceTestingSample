package com.example.rogosample.UI.smart.schedule

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DateAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementSelectionAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentAddSmartScheduleBinding
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.Date
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartSchedule
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd

class AddSmartScheduleFragment : BaseFragment<FragmentAddSmartScheduleBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_add_smart_schedule
    private var deviceList = listOf<IoTDevice>()
    private val dateList = arrayListOf<Date>(
        Date(0),
        Date(1),
        Date(2),
        Date(3),
        Date(4),
        Date(5),
        Date(6)
    )
    private val dateSelectedList = arrayListOf<Int>()
    private val cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private val elementMap = hashMapOf<Int, String>()
    private var ioTTargetCmd: IoTTargetCmd?= null
    /*
    * To add or remove date
    * */
    private val dateAdapter by lazy {
        DateAdapter(isChecked = {
            if(dateSelectedList.contains(it.first)) {
                if(!it.second) {
                    dateSelectedList.remove(it.first)
                }
            } else {
                if(it.second) {
                    dateSelectedList.add(it.first)
                }
            }
        })
    }
    private val commandSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }

    /*
    * To add or remove element
    * */
    private val elementSelectionAdapter by lazy {
        ElementSelectionAdapter(isChecked = {
            when(binding.spinnerCommand.selectedItem as Command) {
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
                        intArrayOf(Command.BRIGHTNESS.cmdAttribute, binding.sbBrightness.progress, binding.sbKelvin.progress)
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
            if(cmdsMap.keys.contains(it.first)) {
                if(!it.second) {
                    cmdsMap.remove(it.first)
                }
            } else {
                if(it.second) {
                    cmdsMap[it.first] = ioTTargetCmd!!
                }
            }
        })
    }
    private lateinit var deviceSpinnerAdapter: DeviceSpinnerAdapter

    private val hourSpinnerAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..12).toList())
    }

    private val minuteSpinnerAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0..60).toList())
    }


    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_smart_schedule)
            rvDate.adapter = dateAdapter
            dateAdapter.submitList(dateList)
            spinnerCommand.adapter = commandSpinnerAdapter
            rvElement.adapter = elementSelectionAdapter
            spinnerHour.adapter = hourSpinnerAdapter
            spinnerMinute.adapter = minuteSpinnerAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            dateSelectedList.clear()
            /*
            * Select command and show devices support that command
            * */
            spinnerCommand.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        val ioTCommand = it.getItemAtPosition(p2) as Command
                        when(ioTCommand) {
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
                        deviceList = SmartSdk.deviceHandler().all.filter {device ->
                            device.containtFeature(ioTCommand.cmdAttribute)
                        }
                        deviceSpinnerAdapter = DeviceSpinnerAdapter(requireContext(), deviceList)
                        spinnerDevice.adapter = deviceSpinnerAdapter
                        if(deviceList.isNullOrEmpty()) {
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
            * To get elements of device, check whether user updated element's label or not
            * */
            spinnerDevice.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elementMap.clear()
                        cmdsMap.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        if(ioTDevice.elementIds.isNotEmpty()) {
                            ioTDevice.elementInfos.forEach{entry ->
                                if(entry.value.label.isNullOrEmpty()) {
                                    elementMap[entry.key] = "Nút ${ioTDevice.getIndexElement(entry.key)}"
                                } else {
                                    elementMap[entry.key] = entry.value.label
                                }
                            }
                            elementSelectionAdapter.submitList(elementMap.entries.toList())
                        } else {
                            ILogR.D("getElement", "No element")
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * To add Smart Schedule, create smart schedule
            * */
            btnAddSchedule.setOnClickListener {
                dateSelectedList.toIntArray().forEach {
                    ILogR.D("dateSelect", it)
                }
                SmartSdk.smartFeatureHandler().createSmartSchedule(
                    edtSmartName.text.toString(),
                    null,
                    object: RequestCallback<IoTSmart> {
                        override fun onSuccess(p0: IoTSmart?) {
                            /*
                            * Bind date and time to active to SmartSchedule
                            * */
                            val uuid = p0?.uuid
                            val time = (spinnerHour.selectedItem as Int) * 60 + (spinnerMinute.selectedItem as Int)
                            SmartSdk.smartFeatureHandler().bindSmartSchedule(
                                uuid,
                                time,
                                dateSelectedList.toIntArray(),
                                object : RequestCallback<IoTSmartSchedule> {
                                    override fun onSuccess(p0: IoTSmartSchedule?) {
                                        /*
                                        * Bind SmartCmd to Smart Schedule
                                        * */
                                        SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                                            uuid,
                                            (spinnerDevice.selectedItem as IoTDevice).uuid,
                                            cmdsMap,
                                            object : RequestCallback<IoTSmartCmd> {
                                                override fun onSuccess(p0: IoTSmartCmd?) {
                                                    showNoti(R.string.add_success)
                                                }

                                                override fun onFailure(p0: Int, p1: String?) {
                                                    p1?.let {
                                                        showNoti(it)
                                                    }
                                                }

                                            }
                                        )
                                    }

                                    override fun onFailure(p0: Int, p1: String?) {
                                        p1?.let {
                                            showNoti(it)
                                        }
                                    }

                                }
                            )
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