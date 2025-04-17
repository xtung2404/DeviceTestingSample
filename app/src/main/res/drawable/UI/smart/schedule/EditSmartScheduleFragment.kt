package com.example.rogosample.UI.smart.schedule

import android.view.View
import android.widget.AdapterView
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import com.example.rogosample.`object`.Date
import com.example.rogosample.R
import com.example.rogosample.adapter.CommandSpinnerAdapter
import com.example.rogosample.adapter.DateAdapter
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.ElementCheckAdapter
import com.example.rogosample.adapter.SmartSpinnerAdapter
import com.example.rogosample.adapter.TimeSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentEditSmartScheduleBinding
import com.example.rogosample.`object`.Command
import com.example.rogosample.`object`.ELement
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmart
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartCmd
import rogo.iot.module.rogocore.sdk.entity.smart.IoTSmartSchedule
import rogo.iot.module.rogocore.sdk.entity.smart.IoTTargetCmd


class EditSmartScheduleFragment : BaseFragment<FragmentEditSmartScheduleBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_edit_smart_schedule

    private var deviceList = listOf<IoTDevice>()
    private val elementList = arrayListOf<ELement>()
    private var cmdsMap = hashMapOf<Int, IoTTargetCmd>()
    private var ioTSmart: IoTSmart? = null
    private val dateList = arrayListOf<Date>(
        Date(0),
        Date(1),
        Date(2),
        Date(3),
        Date(4),
        Date(5),
        Date(6)
    )
    private var deviceUuid: String? = null
    private var dateSelectedList = mutableListOf<Int>()
    private var ioTTargetCmd: IoTTargetCmd? = null

    /*
    * Get list of Smart Schedule
    * */
    private val smartSpinnerAdapter by lazy {
        SmartSpinnerAdapter(
            requireContext(), SmartSdk.smartFeatureHandler().getSmartByType(
                IoTSmartType.TYPE_SCHEDULE
            ).filter {
                SmartSdk.smartFeatureHandler().getSmartSchedule(it.uuid).isNotEmpty()
            }.toList()
        )
    }
    private val commandSpinnerAdapter by lazy {
        CommandSpinnerAdapter(requireContext(), Command.getCmdList())
    }

    private val hourSpinnerAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 24).toList())
    }

    private val minuteSpinnerAdapter by lazy {
        TimeSpinnerAdapter(requireContext(), (0 until 60).toList())
    }


    /*
    * To add or remove element
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

    /*
    * To add or remove date
    * */
    private val dateAdapter by lazy {
        DateAdapter(isChecked = {
            if (dateSelectedList.contains(it.first)) {
                if (!it.second) {
                    dateSelectedList.remove(it.first)
                }
            } else {
                if (it.second) {
                    dateSelectedList.add(it.first)
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
            toolbar.txtTitle.text = resources.getString(R.string.edit_smart_schedule)
            spinnerSmart.adapter = smartSpinnerAdapter
            spinnerCommand.adapter = commandSpinnerAdapter
            rvElement.adapter = elementCheckAdapter
            rvDate.adapter = dateAdapter
            dateAdapter.submitList(dateList)
            spinnerHour.adapter = hourSpinnerAdapter
            spinnerMinute.adapter = minuteSpinnerAdapter
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
            dateSelectedList.clear()
            /*
            * After selecting a command, get the command and the value of the command.
            * Get the device that used for Smart Schedule
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
                                    deviceSpinnerAdapter =
                                        DeviceSpinnerAdapter(requireContext(), deviceList)
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
            * After selecting a Smart Schedule, get the SmartCmd of the of SmartSchedule.
            * Get the date and time of Smart Schedule.
            * Get the device used for Smart Schedule
            * */
            spinnerSmart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        ioTSmart = (it.getItemAtPosition(p2) as IoTSmart)
                        if (SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid)
                                .isNotEmpty()
                        ) {
                            val ioTSmartCmd =
                                SmartSdk.smartFeatureHandler().getSmartCmds(ioTSmart?.uuid).first()
                            deviceUuid = ioTSmartCmd.targetId
                            cmdsMap = ioTSmartCmd.cmds
                            ioTTargetCmd = ioTSmartCmd.cmds.entries.first().value
                            val ioTSmartSchedule =
                                SmartSdk.smartFeatureHandler().getSmartSchedule(ioTSmart?.uuid)
                                    .toList().first()

                            spinnerHour.setSelection(ioTSmartSchedule.appTime / 60)
                            spinnerMinute.setSelection(ioTSmartSchedule.appTime % 60)
                            when (ioTTargetCmd?.cmd?.toList()?.first()) {
                                IoTAttribute.ACT_BRIGHTNESS_KELVIN -> {
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTTargetCmd?.cmd?.toList()?.get(0)
                                                ), true
                                            )
                                            if (SmartSdk.smartFeatureHandler()
                                                    .getSmartCmds(ioTSmart?.uuid)
                                                    .isNotEmpty()
                                            ) {
                                                spinnerDevice.post(object : Runnable {
                                                    override fun run() {
                                                        deviceList =
                                                            SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                        deviceSpinnerAdapter = DeviceSpinnerAdapter(
                                                            requireContext(),
                                                            deviceList
                                                        )
                                                        spinnerDevice.adapter = deviceSpinnerAdapter
                                                        spinnerDevice.setSelection(
                                                            deviceList.indexOf(
                                                                SmartSdk.deviceHandler()
                                                                    .get(deviceUuid)
                                                            ), true
                                                        )
                                                        deviceSpinnerAdapter.notifyDataSetChanged()
                                                    }
                                                })
                                            }
                                            if (deviceList.isNotEmpty()) {
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
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTTargetCmd?.cmd?.toList()?.get(0)
                                                ), true
                                            )
                                            if (SmartSdk.smartFeatureHandler()
                                                    .getSmartCmds(ioTSmart?.uuid)
                                                    .isNotEmpty()
                                            ) {
                                                spinnerDevice.post(object : Runnable {
                                                    override fun run() {
                                                        ILogR.D(
                                                            "tung",
                                                            (deviceList.indexOf(
                                                                SmartSdk.deviceHandler()
                                                                    .get(deviceUuid)
                                                            ))
                                                        )
                                                        deviceList =
                                                            SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                        deviceSpinnerAdapter = DeviceSpinnerAdapter(
                                                            requireContext(),
                                                            deviceList
                                                        )
                                                        spinnerDevice.adapter = deviceSpinnerAdapter
                                                        spinnerDevice.setSelection(
                                                            deviceList.indexOf(
                                                                SmartSdk.deviceHandler()
                                                                    .get(deviceUuid)
                                                            ), true
                                                        )
                                                        deviceSpinnerAdapter.notifyDataSetChanged()
                                                    }
                                                })
                                            }
                                            if (deviceList.isNotEmpty()) {
                                                lnSelectDevice.visibility = View.VISIBLE
                                            } else {
                                                lnSelectDevice.visibility = View.GONE
                                            }
                                            ILogR.D(
                                                "elementCommand",
                                                spinnerCommand.selectedItemPosition
                                            )
                                        }

                                    })
                                    sbHue.progress = ioTTargetCmd?.cmd?.toList()?.get(1)!!
                                    sbSaturation.progress =
                                        ioTTargetCmd?.cmd?.toList()?.get(2)!!
                                }

                                else -> {
                                    lnBrightness.visibility = View.GONE
                                    lnSaturation.visibility = View.GONE
                                    spinnerCommand.post(object : Runnable {
                                        override fun run() {
                                            spinnerCommand.setSelection(
                                                Command.getCmdPos(
                                                    ioTTargetCmd?.cmd?.toList()?.get(0),
                                                    ioTTargetCmd?.cmd?.toList()?.get(1)
                                                ), true
                                            )
                                            if (SmartSdk.smartFeatureHandler()
                                                    .getSmartCmds(ioTSmart?.uuid)
                                                    .isNotEmpty()
                                            ) {
                                                spinnerDevice.post(object : Runnable {
                                                    override fun run() {
                                                        deviceList =
                                                            SmartSdk.deviceHandler().all.filter {
                                                                it.containtFeature((spinnerCommand.selectedItem as Command).cmdAttribute)
                                                            }
                                                        deviceSpinnerAdapter = DeviceSpinnerAdapter(
                                                            requireContext(),
                                                            deviceList
                                                        )
                                                        spinnerDevice.adapter = deviceSpinnerAdapter
                                                        spinnerDevice.setSelection(
                                                            deviceList.indexOf(
                                                                SmartSdk.deviceHandler()
                                                                    .get(deviceUuid)
                                                            ), true
                                                        )
                                                        deviceSpinnerAdapter.notifyDataSetChanged()
                                                    }
                                                })
                                            }
                                            if (deviceList.isNotEmpty()) {
                                                lnSelectDevice.visibility = View.VISIBLE
                                            } else {
                                                lnSelectDevice.visibility = View.GONE
                                            }
                                        }

                                    })

                                }
                            }
                            for (date in dateList) {
                                dateSelectedList = (ioTSmartSchedule.appWeekdays.toMutableList())
                                date.isChecked =
                                    ioTSmartSchedule.appWeekdays.toList().contains(date.dateIndex)
                            }
                            dateAdapter.notifyDataSetChanged()
                        }
                        edtSmartName.setText((it.getItemAtPosition(p2) as IoTSmart).label)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            /*
            * To get elements of device, check whether user updated element's label or not. If not show the index of the element.
            * Check which element is used for Smart Schedule
            * */
            spinnerDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        elementList.clear()
                        val ioTDevice = it.getItemAtPosition(p2) as IoTDevice
                        runBlocking {
                            val job = launch {
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
                            }
                            job.join()
                            for (elm in elementList) {
                                elm.isChecked = cmdsMap.keys.contains(elm.elmIndex)
                            }
                            elementCheckAdapter.submitList(elementList)
                            rvElement.post(object : Runnable {
                                override fun run() {
                                    elementCheckAdapter.notifyDataSetChanged()
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
                * Update lable of Smart Schedule
                * */
                dialogLoading.show()
                val ioTSmart = (spinnerSmart.selectedItem as IoTSmart)
                val label = edtSmartName.text.toString()
                if (!label.contentEquals(ioTSmart.label)) {
                    SmartSdk.smartFeatureHandler().setSmartLabel(
                        ioTSmart.uuid,
                        label,
                        object : RequestCallback<IoTSmart> {
                            override fun onSuccess(p0: IoTSmart?) {
                                /*
                                * Bind date and time to Smart Schedule
                                * */
                                val time =
                                    (spinnerHour.selectedItem as Int) * 60 + (spinnerMinute.selectedItem as Int)
                                SmartSdk.smartFeatureHandler().reboundSmartSchedule(
                                    ioTSmart.uuid,
                                    SmartSdk.smartFeatureHandler().getSmartSchedule(ioTSmart.uuid)
                                        .first().uuid,
                                    time,
                                    dateSelectedList.toIntArray(),
                                    object : RequestCallback<IoTSmartSchedule> {
                                        override fun onSuccess(p0: IoTSmartSchedule?) {
                                            SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                                                ioTSmart.uuid,
                                                (spinnerDevice.selectedItem as IoTDevice).uuid,
                                                cmdsMap,
                                                object : RequestCallback<IoTSmartCmd> {
                                                    override fun onSuccess(p0: IoTSmartCmd?) {
                                                        dialogLoading.dismiss()
                                                        showNoti(R.string.update_success)
                                                    }

                                                    override fun onFailure(
                                                        p0: Int,
                                                        p1: String?
                                                    ) {
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

                            override fun onFailure(p0: Int, p1: String?) {
                                dialogLoading.dismiss()
                                p1?.let {
                                    showNoti(it)
                                }
                            }
                        }
                    )
                } else {
                    val time =
                        (spinnerHour.selectedItem as Int) * 60 + (spinnerMinute.selectedItem as Int)
                    SmartSdk.smartFeatureHandler().reboundSmartSchedule(
                        ioTSmart.uuid,
                        SmartSdk.smartFeatureHandler().getSmartSchedule(ioTSmart.uuid).first().uuid,
                        time,
                        dateSelectedList.toIntArray(),
                        object : RequestCallback<IoTSmartSchedule> {
                            override fun onSuccess(p0: IoTSmartSchedule?) {
                                SmartSdk.smartFeatureHandler().bindDeviceSmartCmd(
                                    ioTSmart.uuid,
                                    (spinnerDevice.selectedItem as IoTDevice).uuid,
                                    cmdsMap,
                                    object : RequestCallback<IoTSmartCmd> {
                                        override fun onSuccess(p0: IoTSmartCmd?) {
                                            showNoti(R.string.update_success)
                                        }

                                        override fun onFailure(
                                            p0: Int,
                                            p1: String?
                                        ) {
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
}