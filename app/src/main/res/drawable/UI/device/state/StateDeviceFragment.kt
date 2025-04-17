package com.example.rogosample.UI.device.state

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceStateAdapter
import com.example.rogosample.adapter.ElementSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentStateDeviceBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.SuccessRequestCallback
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTCmdConst
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.SmartSdkDeviceStateCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import java.util.Arrays
import java.util.Calendar

class StateDeviceFragment : BaseFragment<FragmentStateDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_state_device
    private val TAG = "StateDeviceFragment"
    private var ioTDevice: IoTDevice? = null
    private lateinit var elementSpinnerAdapter: ElementSpinnerAdapter
    private val elementMap = hashMapOf<Int, String>()
    private val deviceStateAdapter: DeviceStateAdapter by lazy {
        DeviceStateAdapter()
    }
    private val smartDeviceStateCallback: SmartSdkDeviceStateCallback = object : SmartSdkDeviceStateCallback() {
        override fun onDeviceStateUpdated(p0: String?) {
            ILogR.D(TAG, "onDeviceStateUpdated")
        }

        override fun onEventAttrStateChange(p0: String?, p1: Int, p2: IntArray?) {
            ILogR.D(TAG, "onEventAttrStateChange", p0, p1, Arrays.toString(p2))
            CoroutineScope(Dispatchers.Main).launch {
                when(p2!![0]) {
                    IoTAttribute.ACT_ONOFF -> {
                        binding.lnCurrentState.visibility = View.VISIBLE
                        binding.txtCurrentState.text = if (p2[1] == IoTCmdConst.POWER_ON) context?.getString(R.string.power_on) else context?.getString(R.string.power_off)
                    }
                    IoTAttribute.ACT_OPEN_CLOSE -> {
                        binding.lnCurrentState.visibility = View.VISIBLE
                        binding.txtCurrentState.text = if (p2[1] == IoTCmdConst.OPENCLOSE_MODE_OPEN) context?.getString(R.string.open) else context?.getString(R.string.close)
                    }
                    IoTAttribute.ACT_LOCK_UNLOCK -> {
                        binding.lnCurrentState.visibility = View.VISIBLE
                        binding.txtCurrentState.text = if (p2[1] == IoTCmdConst.DOOR_LOCKED) context?.getString(R.string.lock) else context?.getString(R.string.unlock)
                    }
                    IoTAttribute.EVT_ONLINE_STATUS -> {
                        binding.lnOnline.visibility = View.VISIBLE
                        binding.txtOnline.text = if (p2[1] == 1) "true" else "false"
                    }
                    IoTAttribute.EVT_BATTERY -> {
                        binding.lnCurrentBattery.visibility = View.VISIBLE
                        binding.txtCurrentBattery.text = p2[1].toString()
                    }
                    IoTAttribute.EVT_LUX -> {
                        binding.txtCurrentLux.visibility = View.VISIBLE
                        binding.txtHasIssues.text = p2[1].toString()
                    }
                    IoTAttribute.INFO_CURRENT_ISSUES_STATUS -> {
                        binding.lnHasIssues.visibility = View.VISIBLE
                        binding.txtHasIssues.text = if (p2[1] == 1) "true" else "false"
                    }
                    IoTAttribute.EVT_PRESENCE -> {
                        binding.lnCurrentState.visibility = View.VISIBLE
                        binding.txtCurrentState.text = if (p2[1] == 1) "PRESENCE" else "UNPRESENCE"
                    }
                    IoTAttribute.EVT_SMOKE -> {
                        binding.lnCurrentState.visibility = View.VISIBLE
                        binding.txtCurrentState.text = if (p2[1] == 1) "SMOKE" else "NOT SMOKE"
                    }
                    IoTAttribute.EVT_TEST_ALARM -> {
                        binding.lnTestAlarm.visibility = View.VISIBLE
                        binding.txtTestAlarm.text = p2[2].toString()
                    }
                    IoTAttribute.EVT_WALL_MOUNTED -> {
                        binding.lnWallMount.visibility = View.VISIBLE
                        binding.txtWallMount.text = if (p2[1] == 1) "true" else "false"
                    }
                }
            }
        }

        override fun onEventAttrStateControl(p0: String?, p1: Int, p2: IntArray?) {
            ILogR.D(TAG, "onEventAttrStateControl")
        }

        override fun onDeviceLogSensorUpdated(p0: String?, p1: Int, p2: Int) {
            ILogR.D(TAG, "onDeviceLogSensorUpdated")
        }
    }
    override fun initVariable() {
        super.initVariable()
        binding.apply {
            arguments?.let {
                ioTDevice = SmartSdk.deviceHandler().get(it.getString("uuid"))
            }
            ioTDevice?.let {
                ILogR.D(TAG, it.productId, it.devType, Arrays.toString(it.features))
                ILogR.D(TAG, Gson().toJson(it))
                it.elementInfos.entries.forEach {
                    ILogR.D(TAG, it.key, it.value.label, it.value.devType, Arrays.toString(it.value.attrs))
                }
                ILogR.D(TAG, "PRODUCT_MODEL", Gson().toJson(SmartSdk.getProductModel(it.productId)))
                SmartSdk.registerDeviceStateCallback(smartDeviceStateCallback)
                CoroutineScope(Dispatchers.Main).launch {
                    when (it.devType) {
                        IoTDeviceType.GATEWAY -> {
                            lnStateSubDevices.visibility = View.VISIBLE
                            rvSubDevicesState.adapter = deviceStateAdapter
                            pingStateSubDevices()
//                            pingDeviceState()

                        }
                        else -> {
                            pingDeviceState()
                        }
                    }
                    when(it.devType) {
                        IoTDeviceType.GATE,
                        IoTDeviceType.PRESENSCE_SENSOR,
                        IoTDeviceType.SMOKE_SENSOR,
                        IoTDeviceType.MOTION_SENSOR,
                        IoTDeviceType.DOORLOCK,
                        IoTDeviceType.DOOR_SENSOR -> {
                            lnLogSensor.visibility = View.VISIBLE
                            pingLogSensor()
                        }
                        else -> {

                        }

                    }
                }
            }
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
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerElement.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    parent?.let {
                        val elm = it.selectedItem as Map.Entry<Int, String>
                        pingDeviceState(elm.key)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }
    }

    private fun pingDeviceState() {
        SmartSdk.stateHandler().pingDeviceState(
            ioTDevice!!.uuid,
            object : SuccessRequestCallback {
                override fun onSuccess() {
                    ILogR.D("Ping state", "onSuccess")
                    CoroutineScope(Dispatchers.Main).launch {
                        val currentState = SmartSdk.stateHandler().getObjState(ioTDevice!!.uuid)
                        ILogR.D(TAG, "CURRENT_STATE", Gson().toJson(currentState), currentState.isOn)
                        binding.apply {
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_ONLINE_STATUS)) {
                                lnOnline.visibility = View.VISIBLE
                                txtOnline.text = if (currentState.getOnlineStatus(ioTDevice!!.elementIds.first()) == 1) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_BATTERY)) {
                                lnCurrentBattery.visibility = View.VISIBLE
                                txtCurrentBattery.text = currentState.battery.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_LUX)) {
                                lnCurrentLux.visibility = View.VISIBLE
                                txtCurrentLux.text = currentState.lux.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.INFO_CURRENT_ISSUES_STATUS)) {
                                lnHasIssues.visibility = View.VISIBLE
                                txtHasIssues.text = if (currentState.isHasIssues) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_TEMP)) {
                                lnCurrentTemp.visibility = View.VISIBLE
                                txtCurrentTemp.text = currentState.temp.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.ACT_ONOFF)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isOn) context?.getString(R.string.power_on) else context?.getString(R.string.power_off)
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_OPEN_CLOSE)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isDoorOpen(ioTDevice!!.elementIds.first())) context?.getString(R.string.open) else context?.getString(R.string.close)
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_WALL_MOUNTED)) {
                                lnWallMount.visibility = View.VISIBLE
                                txtWallMount.text = if (currentState.isWallMounted(ioTDevice!!.elementIds.first())) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.ACT_LOCK_UNLOCK)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isLocked(ioTDevice!!.elementIds.first())) context?.getString(R.string.open) else context?.getString(R.string.close)
                            }
                        }
                    }
                }

                override fun onFailure(p0: Int, p1: String?) {
                    ILogR.D("Ping state", "onFailure", p0, p1)
                }

            })
    }
    private fun pingDeviceState(elm: Int) {
        SmartSdk.stateHandler().pingDeviceState(
            ioTDevice!!.uuid,
            object : SuccessRequestCallback {
                override fun onSuccess() {
                    ILogR.D("Ping state", "onSuccess")
                    CoroutineScope(Dispatchers.Main).launch {
                        val currentState = SmartSdk.stateHandler().getObjState(ioTDevice!!.uuid)
                        ILogR.D(TAG, "CURRENT_STATE", Gson().toJson(currentState), currentState.isOn)
                        binding.apply {
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_ONLINE_STATUS)) {
                                lnOnline.visibility = View.VISIBLE
                                txtOnline.text = if (currentState.getOnlineStatus(ioTDevice!!.elementIds.first()) == 1) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_BATTERY)) {
                                lnCurrentBattery.visibility = View.VISIBLE
                                txtCurrentBattery.text = currentState.battery.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_LUX)) {
                                lnCurrentLux.visibility = View.VISIBLE
                                txtCurrentLux.text = currentState.lux.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.INFO_CURRENT_ISSUES_STATUS)) {
                                lnHasIssues.visibility = View.VISIBLE
                                txtHasIssues.text = if (currentState.isHasIssues) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_TEMP)) {
                                lnCurrentTemp.visibility = View.VISIBLE
                                txtCurrentTemp.text = currentState.temp.toString()
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.ACT_ONOFF)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isOn(elm)) context?.getString(R.string.power_on) else context?.getString(R.string.power_off)
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_OPEN_CLOSE)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isDoorOpen(ioTDevice!!.elementIds.first())) context?.getString(R.string.open) else context?.getString(R.string.close)
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.EVT_WALL_MOUNTED)) {
                                lnWallMount.visibility = View.VISIBLE
                                txtWallMount.text = if (currentState.isWallMounted(ioTDevice!!.elementIds.first())) "true" else "false"
                            }
                            if (ioTDevice!!.containtFeature(IoTAttribute.ACT_LOCK_UNLOCK)) {
                                lnCurrentState.visibility = View.VISIBLE
                                txtCurrentState.text = if (currentState.isLocked(ioTDevice!!.elementIds.first())) context?.getString(R.string.open) else context?.getString(R.string.close)
                            }
                        }
                    }
                }

                override fun onFailure(p0: Int, p1: String?) {
                    ILogR.D("Ping state", "onFailure", p0, p1)
                }

            })
    }

    private fun pingStateSubDevices() {
        SmartSdk.stateHandler().pingDeviceStateWithSubDevice(
            ioTDevice!!.uuid,
            object : SuccessRequestCallback {
                override fun onSuccess() {
                    CoroutineScope(Dispatchers.Main).launch {
                        val stateList = SmartSdk.stateHandler().getSubObjStates(ioTDevice!!.uuid)
                        stateList.forEach {state ->
                            ILogR.D(TAG, "DEVICE_STATE", Gson().toJson(state))
                        }
                        deviceStateAdapter.submitList(stateList.toList())
                    }
                }

                override fun onFailure(p0: Int, p1: String?) {

                }
            })
    }

    private fun pingLogSensor() {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val attr = when(ioTDevice!!.devType) {
            IoTDeviceType.GATE -> IoTAttribute.ACT_OPEN_CLOSE
            IoTDeviceType.MOTION_SENSOR -> IoTAttribute.EVT_MOTION
            IoTDeviceType.DOOR_SENSOR -> IoTAttribute.EVT_OPEN_CLOSE
            IoTDeviceType.PRESENSCE_SENSOR -> IoTAttribute.EVT_PRESENCE
            IoTDeviceType.DOORLOCK -> IoTAttribute.ACT_LOCK_UNLOCK
            else -> IoTAttribute.EVT_SMOKE
        }
        CoroutineScope(Dispatchers.Main).launch {
            SmartSdk.stateHandler().pingLogSensorDevice(
                ioTDevice!!.uuid,
                when(ioTDevice!!.devType) {
                    IoTDeviceType.PRESENSCE_SENSOR -> ioTDevice!!.elementInfos.filter { !it.value.attrs.contains(IoTAttribute.ACT_ONOFF) }.entries.first().key
                    else -> ioTDevice!!.elementIds.first()
                },
                attr
            )
            delay(300)
            val logCollection = SmartSdk.stateHandler().getSensorLogs(
                ioTDevice!!.uuid,
                when(ioTDevice!!.devType) {
                    IoTDeviceType.PRESENSCE_SENSOR -> ioTDevice!!.elementInfos.filter { !it.value.attrs.contains(IoTAttribute.ACT_ONOFF) }.entries.first().key
                    else -> ioTDevice!!.elementIds.first()
                },
                attr,
                getCurrentDate(year, month, day),
                year
                )
        }
    }
    fun getCurrentDate(year: Int, month: Int, day: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar.get(Calendar.DAY_OF_YEAR)
    }
}