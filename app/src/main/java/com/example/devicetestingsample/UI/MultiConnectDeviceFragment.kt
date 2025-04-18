package com.example.devicetestingsample.UI

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.navigation.fragment.findNavController
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.adapter.ConnectedDevicesAdapter
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.UI.localdb.PrefsManager
import com.example.devicetestingsample.databinding.FragmentMultiConnectDeviceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.util.Integers
import rogo.iot.module.rogocore.basesdk.ILogR
import rogo.iot.module.rogocore.basesdk.callback.RequestCallback
import rogo.iot.module.rogocore.basesdk.define.IoTCmdConst
import rogo.iot.module.rogocore.basesdk.utils.ByteUtil
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.FirmwareVersionCheckingCallback
import rogo.iot.module.rogocore.sdk.callback.LearnACIRRawCallback
import rogo.iot.module.rogocore.sdk.callback.SetupDeviceWileCallback
import rogo.iot.module.rogocore.sdk.callback.SmartSdkOTADeviceStateCallback
import rogo.iot.module.rogocore.sdk.callback.SuccessCallback
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTIrProtocolInfo
import rogo.iot.module.rogocore.sdk.entity.IoTIrRemote
import rogo.iot.module.rogocore.sdk.entity.IoTWileScanned
import rogo.iot.module.rogocore.sdk.entity.SetupDeviceInfo


class MultiConnectDeviceFragment : BaseFragment<FragmentMultiConnectDeviceBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_multi_connect_device
    private val TAG = "MultiConnectDeviceFragment"
    var isScanning = false
    val connectedDevices = arrayListOf<IoTDevice>()
    var currentDevice: IoTDevice?= null
    var ioTIrProtocol: IoTIrProtocolInfo?= null
    var ioTIrRemote: IoTIrRemote?= null
    var ioTDeviceRemote: IoTDevice?= null
    private var sharedPrefs: PrefsManager?= null
    val learnIrCodeList = arrayListOf<Int>()
    var isOn = false
    var currentTemp = 24
    private lateinit var btnNavBack :AppCompatImageButton
    private lateinit var btnStart :AppCompatButton
    private lateinit var btnRetry :AppCompatButton
    private lateinit var btnContinue :AppCompatButton
    private lateinit var btnConfirm :AppCompatButton
    private lateinit var txtFirmVer: TextView
    private lateinit var txtInstruction: TextView
    private lateinit var txtResult: TextView
    private lateinit var lnResult : LinearLayout
    private lateinit var lnControl: LinearLayout
    private lateinit var lnContinue: LinearLayout
    private val dialogTestingIr by lazy {
        Dialog(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        dialogTestingIr.window?.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        dialogTestingIr.setContentView(R.layout.dialog_testing_ir_device)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDialogTestingIr()
    }
    private val connectedDevicesAdapter: ConnectedDevicesAdapter by lazy {
        ConnectedDevicesAdapter(
            onDeleteDevice = {
                SmartSdk.deviceHandler().delete(
                    it.uuid,
                    object : RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                connectedDevices.remove(it)
                                connectedDevicesAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onFailure(p0: Int, p1: String?) {

                        }

                    })
            },
            onUpdateFirmware = { device, isForceUpdating ->
                CoroutineScope(Dispatchers.Main).launch {
                    binding.apply {
                        if(!isForceUpdating) {
                            if (!switchUpdateFirmware.isChecked) return@launch
                        }
                        SmartSdk.registerOTADeviceStateCallback(otaDeviceStateCallback)
                        SmartSdk.deviceHandler().requestOta(
                            device.uuid,
                            object : RequestCallback<Boolean> {
                                override fun onSuccess(p0: Boolean?) {
                                    ILogR.D(TAG, "ON_UPDATE_OTA_SUCCESS")
                                }

                                override fun onFailure(p0: Int, p1: String?) {

                                }

                            }
                        )
                    }
                }
            },
            onTesting = {
                currentDevice = it
                onStartTesting()
            }
        )
    }

    private val otaDeviceStateCallback: SmartSdkOTADeviceStateCallback = object : SmartSdkOTADeviceStateCallback() {
        override fun onOTAProgress(deviceId: String?, progress: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                deviceId?.let {
                    connectedDevicesAdapter.updateProgress(it, progress)
                }
            }
        }

        override fun onSuccess(deviceId: String?) {
            ILogR.D(TAG, "ON_UPDATE_OTA_SUCCESS_CALLBACK")
            CoroutineScope(Dispatchers.Main).launch {
                deviceId?.let {
                    connectedDevicesAdapter.updateVersion(it)
                }
            }
        }

        override fun onFailure(deviceId: String?, errCode: Int, msg: String?) {

        }

    }

    override fun initVariable() {
        super.initVariable()
        isScanning = false
        connectedDevices.clear()
        binding.apply {
            rvConnectedDevices.adapter = connectedDevicesAdapter
            connectedDevicesAdapter.submitList(connectedDevices)
            lnSetting.visibility = View.GONE
            txtLabel.text = ""
            txtProductid.text = ""
            txtPercent.text = ""
            sharedPrefs = PrefsManager(requireContext())
            sharedPrefs?.let {
                edtSsid.setText(it.getString(KEY_WIFI_SSID))
                edtPassword.setText(it.getString(KEY_WIFI_PASSWORD))
                switchConnect.isChecked = it.getBoolean(KEY_CONNECT)
                switchUpdateFirmware.isChecked = it.getBoolean(KEY_UPDATE_FIRMWARE)
                switchDeleteEnable.isChecked = it.getBoolean(KEY_DELTETE_NOT_OTA)
                switchDeleteTest.isChecked = it.getBoolean(KEY_DELETE_NOT_TESTED)
                edtProductid.setText(it.getString(KEY_PRODUCTID))
            }
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnBack.setOnClickListener {
                stopScan()
                findNavController().navigate(R.id.dashboardFragment)
            }

            btnSettings.setOnClickListener {
                if (lnSetting.visibility == View.GONE) {
                    lnSetting.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                lnSetting.visibility = View.GONE
            }

            edtSsid.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { ssid ->
                        sharedPrefs?.setString(KEY_WIFI_SSID, ssid.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { wifiPass ->
                        sharedPrefs?.setString(KEY_WIFI_PASSWORD, wifiPass.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            edtProductid.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        s?.let {
                            sharedPrefs?.setString(KEY_PRODUCTID, s.toString())
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                }
            )

            btnDelete.setOnClickListener {
                connectedDevices.forEach {
                    SmartSdk.deviceHandler().delete(
                        it.uuid,
                        object : RequestCallback<Boolean> {
                            override fun onSuccess(p0: Boolean?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    ILogR.D(TAG, "ON_DELETE_DEVICE", "ON_SUCCESS")
                                    connectedDevices.remove(it)
                                    connectedDevicesAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onFailure(p0: Int, p1: String?) {

                            }

                        }
                    )
                }
            }

            btnStartScan.setOnClickListener {
                if (isScanning) {
                    stopScan()
                    resetUI()
                    btnStartScan.text = context?.resources?.getString(R.string.start_scanning_for_devices)
                    isScanning = false
                } else {
                    btnStartScan.text = context?.resources?.getString(R.string.stop_scanning)
                    isScanning = true
                    connectedDevices.clear()
                    startScan()
                }
            }
            switchConnect.setOnCheckedChangeListener(
                object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        sharedPrefs?.setBoolean(KEY_CONNECT, isChecked)
                    }

                }
            )

            switchUpdateFirmware.setOnCheckedChangeListener(
                object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        sharedPrefs?.setBoolean(KEY_UPDATE_FIRMWARE, isChecked)
                    }

                }
            )
        }
    }

    fun startScan() {
        SmartSdk.configWileHandler().discoveryWileDevice {
            it?.let { ioTWileScanned ->
                if (ioTWileScanned.ioTProductModel.modelId.contentEquals(binding.edtProductid.text)) {
                    startConfig(it)
                    stopScan()
                }
            }
        }
    }

    fun startConfig(
        ioTWileScanned: IoTWileScanned
    ) {
        binding.apply {
            val scannedModel = SmartSdk.getProductModel(ioTWileScanned.ioTProductModel.modelId)
            scannedModel?.let {
                txtLabel.text = it.name
                txtProductid.text = it.modelId
            }
        }
        val setUpDeviceInfo = SetupDeviceInfo(
            ioTWileScanned.device.address,
            ioTWileScanned.device.name,
            null,
            ioTWileScanned.ioTProductModel.modelId
        )
        SmartSdk.configWileHandler().startSetupWileDevice(
            setUpDeviceInfo,
            object : SetupDeviceWileCallback {
                override fun onProgress(id: String?, progress: Int, msg: String?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.txtPercent.text = "${progress} %"
                        if (progress == 20) {
                            SmartSdk.configWileHandler().setWifiPwd(
                                binding.edtSsid.text.toString(),
                                binding.edtPassword.text.toString(),
                                true
                            )
                        }
                    }
                }

                override fun onSuccess(ioTDevice: IoTDevice?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.apply {
                            txtLabel.text = ""
                            txtProductid.text = ""
                            txtPercent.text = ""
                        }
                        ioTDevice?.let {
                            connectedDevices.add(0, it)
                            connectedDevicesAdapter.notifyItemInserted(0)
                            binding.txtNumberConnectedDevices.text = context?.resources?.getString(R.string.number_of_connected_devices) + ": ${connectedDevices.size}"
                            binding.txtNumberUpdatedDevices.text = context?.resources?.getString(R.string.number_of_connected_devices) + ": ${connectedDevices.size}"
                            resetUI()
                            if (!binding.switchConnect.isChecked) {
                                stopScan()
                            } else {
                                startScan()
                            }
                        }
                    }
                }

                override fun onSetupFailure(errorCode: Int, msg: String) {

                }

                override fun onWifiScanned(ssid: String, auth: Int, rssi: Int) {

                }

                override fun onWifiStopScanned() {

                }

                override fun onWifiSetted() {

                }

                override fun onWifiSsidInfo(status: Int, ssid: String?) {

                }

            }
        )
    }

    fun resetUI() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                txtLabel.text = ""
                txtProductid.text = ""
                txtPercent.text = ""
            }
        }
    }

    fun stopScan() {
        SmartSdk.configWileHandler().stopDiscovery()
    }

    private fun setUpDialogTestingIr() {
        btnNavBack = dialogTestingIr.findViewById<AppCompatImageButton>(R.id.btn_back)
        btnStart = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_start)
        btnRetry = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_retry)
        btnContinue = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_continue)
        btnConfirm = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_confirm)
        txtFirmVer = dialogTestingIr.findViewById<TextView>(R.id.txt_firm_ver)
        txtInstruction = dialogTestingIr.findViewById<TextView>(R.id.txt_instruction)
        txtResult = dialogTestingIr.findViewById<TextView>(R.id.txt_result)
        lnResult = dialogTestingIr.findViewById<LinearLayout>(R.id.ln_result)
        lnControl = dialogTestingIr.findViewById<LinearLayout>(R.id.ln_control)
        lnContinue = dialogTestingIr.findViewById<LinearLayout>(R.id.ln_continue)
        val btnOff = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_off)
        val btn25 = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_25)
        val btn26 = dialogTestingIr.findViewById<AppCompatButton>(R.id.btn_26)
        btnNavBack.setOnClickListener {
            SmartSdk.learnIrDeviceHandler().stopLearnIr()
            dialogTestingIr.dismiss()
        }
        btnStart.setOnClickListener {
            learnIrCodeList.clear()
            currentTemp = 24
            btnStart.visibility = View.GONE
            learnIr()
            lnResult.visibility = View.VISIBLE
        }

        btnRetry.setOnClickListener {
            lnContinue.visibility = View.GONE
            btnContinue.visibility = View.GONE
            btnRetry.visibility = View.GONE
            learnIr()
        }
        btnContinue.setOnClickListener {
            isOn = true
            currentTemp += 1
            txtInstruction.text = "Turn on the remote, aim remote at IR and choose mode Cooling, temp = ${currentTemp}"
            lnContinue.visibility = View.GONE
            btnContinue.visibility = View.GONE
            btnRetry.visibility = View.GONE
            learnIr()
        }

        btnOff.setOnClickListener {
            ioTDeviceRemote?.let {
                SmartSdk.controlHandler().controlDevicePower(
                    it.uuid,
                    false,
                    null
                )
            }
        }

        btn25.setOnClickListener {
            ioTDeviceRemote?.let {
                SmartSdk.controlHandler().controlIr(
                    it.uuid,
                    convertKeyCode(
                        25,
                        IoTCmdConst.AC_MODE_COOLING,
                        IoTCmdConst.FAN_SPEED_LOW
                    ),
                    null
                )
            }
        }

        btn26.setOnClickListener {
            ioTDeviceRemote?.let {
                SmartSdk.controlHandler().controlIr(
                    it.uuid,
                    convertKeyCode(
                        26,
                        IoTCmdConst.AC_MODE_COOLING,
                        IoTCmdConst.FAN_SPEED_LOW
                    ),
                    null
                )
            }
        }

        btnConfirm.setOnClickListener {
            btnConfirm.visibility = View.GONE
            txtInstruction.text = "Waiting to control the remote"
            SmartSdk.learnIrDeviceHandler().addAcIrRawRemote(
                currentDevice!!.uuid,
                "remote ${currentDevice!!.mac}",
                null,
                ioTIrProtocol!!.manufacturer,
                ioTIrProtocol!!.irp,
                IoTIrRemote().apply {
                    this.fans = intArrayOf(IoTCmdConst.FAN_SPEED_LOW)
                    this.modes = intArrayOf(IoTCmdConst.AC_MODE_COOLING)
                    this.tempRange = intArrayOf(24, 25, 26)
                    this.fanAllowIn = intArrayOf(IoTCmdConst.AC_MODE_COOLING)
                    this.tempAllowIn = intArrayOf(IoTCmdConst.AC_MODE_COOLING)
                },
                object : SuccessCallback<IoTDevice> {
                    override fun onFailure(errorCode: Int, message: String?) {

                    }

                    override fun onSuccess(item: IoTDevice?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            ioTDeviceRemote = item
                            txtInstruction.text = ""
                            btnConfirm.visibility = View.GONE
                            lnContinue.visibility = View.GONE
                            lnControl.visibility = View.VISIBLE
                        }
                    }
                }
            )
        }



        dialogTestingIr.setCanceledOnTouchOutside(false)
        val window = dialogTestingIr.window ?: return
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun onStartTesting() {
        btnStart.visibility = View.VISIBLE
        lnResult.visibility = View.GONE
        lnContinue.visibility = View.GONE
        btnConfirm.visibility = View.GONE
        btnContinue.visibility = View.GONE
        btnRetry.visibility = View.GONE
        lnControl.visibility = View.GONE
        txtInstruction.text = context?.resources?.getString(R.string.point_the_control_at_the_ir)
        txtFirmVer.text = ""
        txtResult.text = ""
        dialogTestingIr.show()
        SmartSdk.deviceHandler().checkNewFirmVersion(
            currentDevice!!.uuid,
            object : FirmwareVersionCheckingCallback {
                override fun onSuccess(currentVer: String?, newVer: String?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        txtFirmVer.text = "Current version: ${currentVer}"
                    }
                }

                override fun onFailure(currentVer: String?, code: Int, msg: String?) {
                    txtFirmVer.text = ""
                }
            }
        )
    }
    fun learnIr() {
        txtResult.text = "Waiting for IR to receive signal...."
        SmartSdk.learnIrDeviceHandler().learnAcIrRawRemote(
            currentDevice!!.uuid,
            object : LearnACIRRawCallback {
                override fun onRequestLearnIrStatus(success: Boolean) {

                }

                override fun onIrRawLearned(irProtocol: IoTIrProtocolInfo?) {
                    if (irProtocol != null) {
                        ioTIrProtocol = irProtocol
                        SmartSdk.learnIrDeviceHandler().setACKeyLearn(
                            irProtocol,
                            true,
                            currentTemp,
                            IoTCmdConst.AC_MODE_COOLING,
                            IoTCmdConst.FAN_SPEED_LOW,
                            object : RequestCallback<Int> {
                                override fun onSuccess(p0: Int?) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        p0?.let {
                                            learnIrCodeList.add(it)
                                        }
                                        if (learnIrCodeList.size == 3) {
                                            lnResult.visibility = View.GONE
                                            btnConfirm.visibility = View.VISIBLE
                                            return@launch
                                        }
                                        txtResult.text = "Receive success"
                                        lnContinue.visibility = View.VISIBLE
                                        btnContinue.visibility = View.VISIBLE
                                    }

                                }

                                override fun onFailure(p0: Int, p1: String?) {

                                }

                            }
                        )
                    } else {
                        txtResult.text = "Receive failure"
                        lnContinue.visibility = View.VISIBLE
                        btnRetry.visibility = View.VISIBLE
                    }
                }

                override fun onIrProtocolDetected(irProtocol: IoTIrProtocolInfo?) {

                }

                override fun onFailure(errorCode: Int, msg: String?) {
                    txtResult.text = "Receive failure ${errorCode} ${msg}"
                    lnContinue.visibility = View.VISIBLE
                    btnRetry.visibility = View.VISIBLE
                }
            }
        )
    }
    companion object {
        val KEY_WIFI_SSID = "WIFI_SSID"
        val KEY_WIFI_PASSWORD = "WIFI_PASSWORD"
        val KEY_PRODUCTID = "ProductId"
        val KEY_CONNECT = "CONNECT_DEVICE"
        val KEY_UPDATE_FIRMWARE = "UPDATE_FIRMWARE"
        val KEY_DELETE_NOT_TESTED = "DELETE_NOT_TESTED"
        val KEY_DELTETE_NOT_OTA = "DELETE_NOT_OTA"
    }

    fun convertKeyCode(temp: Int, mode: Int, fan: Int): Int {
        var binary = "1" + ByteUtil.getBinary(mode, 3)
        binary += ByteUtil.getBinary(temp - 15, 4)
        binary += ByteUtil.getBinary(fan, 3)
        binary += "11111"
        return ByteUtil.getUShort(binary)
    }
}