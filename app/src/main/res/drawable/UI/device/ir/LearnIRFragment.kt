package com.example.rogosample.UI.device.ir

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.DeviceSpinnerAdapter
import com.example.rogosample.adapter.GroupSpinnerAdapter
import com.example.rogosample.adapter.IrCodeAdapter
import com.example.rogosample.adapter.LearnIrDeviceAdapter
import com.example.rogosample.adapter.ManufacturerSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.base.getDeviceName
import com.example.rogosample.databinding.FragmentLearnIRBinding
import com.example.rogosample.`object`.DeviceLearnIr
import com.example.rogosample.`object`.IrCode
import com.example.rogosample.`object`.getIrCodeName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rogo.iot.module.platform.ILogR
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.basesdk.define.IoTIrCode
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.CheckIrHubAvailableCallback
import rogo.iot.module.rogocore.sdk.callback.LearnIrCallback
import rogo.iot.module.rogocore.sdk.define.ir.IoTIrPrtc
import rogo.iot.module.rogocore.sdk.entity.IoTDevice
import rogo.iot.module.rogocore.sdk.entity.IoTGroup
import rogo.iot.module.rogocore.sdk.entity.IoTIrProtocolInfo

class LearnIRFragment : BaseFragment<FragmentLearnIRBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_learn_i_r

    private val irDeviceList = arrayListOf<IoTDevice>()
    private val irList = arrayListOf<IrCode>()
    private val irCodeToLearn = arrayListOf<Int>()
    private val irCodeLearned = arrayListOf<Int>()
    private var currentPos = -1
    private var protocolInfo: IoTIrProtocolInfo? = null
    private lateinit var txtLearnIr: TextView
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnRetry: AppCompatButton
    private lateinit var btnCancel: AppCompatButton
    private val learnIrDeviceAdapter by lazy {
        LearnIrDeviceAdapter(requireContext(), DeviceLearnIr.getDeviceTypes())
    }
    private val iRDeviceAdapter by lazy {
        DeviceSpinnerAdapter(requireContext(), irDeviceList)
    }
    private val irCodeAdapter by lazy {
        IrCodeAdapter()
    }
    private val learnIrDialog by lazy {
        Dialog(requireContext())
    }
    private val setUpRemoteDialog by lazy {
        Dialog(requireContext())
    }

    private val groupSpinnerAdapter by lazy {
        GroupSpinnerAdapter(requireContext(), SmartSdk.groupHandler().all.toMutableList())
    }

    private lateinit var manufacturerSpinnerAdapter: ManufacturerSpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        learnIrDialog.window?.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        learnIrDialog.setContentView(R.layout.layout_dialog_learn_ir)
        setUpRemoteDialog.window?.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setUpRemoteDialog.setContentView(R.layout.dialog_set_up_remote)
        super.onCreate(savedInstanceState)
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
                SmartSdk.configIrRemoteDeviceHandler().stopLearnIr()
            }
            toolbar.txtTitle.text = resources.getString(R.string.add_ir_remote)
            spinnerType.adapter = learnIrDeviceAdapter
            spinnerHub.adapter = iRDeviceAdapter
            lnDeviceType.visibility = View.GONE
            rvIr.adapter = irCodeAdapter
            irCodeAdapter.submitList(irList)
            setUpDialogLearnIr()
            setUpDialogSetUpRemote()
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            irDeviceList.clear()
            /*
            * Check available Hub devices such as : FPT Box, IR Device...
            * */
            SmartSdk.configIrRemoteDeviceHandler()
                .checkIrHubAvailable(object : CheckIrHubAvailableCallback {
                    override fun onIrDeviceInfo(
                        devId: String?,
                        irSupport: Int,
                        protcolSupport: Int
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            irDeviceList.add(SmartSdk.deviceHandler().get(devId))
                            iRDeviceAdapter.notifyDataSetChanged()
                        }
                    }
                })

            /*
            * Select available buttons like the remote
            * */
            spinnerType.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    p0?.let {
                        when (it.getItemAtPosition(p2)) {
                            DeviceLearnIr.FAN -> {
                                lnFanTv.visibility = View.VISIBLE
                                lnAc.visibility = View.GONE
                                irList.clear()
                                for (i in listOf<Int>(
                                    IoTIrCode.POWER,
                                    IoTIrCode.POWER_ON,
                                    IoTIrCode.POWER_OFF,
                                    IoTIrCode.NUM_0,
                                    IoTIrCode.NUM_1,
                                    IoTIrCode.NUM_2,
                                    IoTIrCode.NUM_3,
                                    IoTIrCode.NUM_4,
                                    IoTIrCode.NUM_5,
                                    IoTIrCode.NUM_6,
                                    IoTIrCode.NUM_7,
                                    IoTIrCode.NUM_8,
                                    IoTIrCode.NUM_9,
                                    IoTIrCode.FAN_SPEED
                                )) {
                                    irList.add(IrCode(i, getIrCodeName(i)))
                                }
                                irCodeAdapter.notifyDataSetChanged()
                            }

                            DeviceLearnIr.TV -> {
                                lnFanTv.visibility = View.VISIBLE
                                lnAc.visibility = View.GONE
                                irList.clear()
                                for (i in listOf<Int>(
                                    IoTIrCode.POWER,
                                    IoTIrCode.POWER_ON,
                                    IoTIrCode.POWER_OFF,
                                    IoTIrCode.NUM_0,
                                    IoTIrCode.NUM_1,
                                    IoTIrCode.NUM_2,
                                    IoTIrCode.NUM_3,
                                    IoTIrCode.NUM_4,
                                    IoTIrCode.NUM_5,
                                    IoTIrCode.NUM_6,
                                    IoTIrCode.NUM_7,
                                    IoTIrCode.NUM_8,
                                    IoTIrCode.NUM_9,
                                    IoTIrCode.UP,
                                    IoTIrCode.DOWN,
                                    IoTIrCode.LEFT,
                                    IoTIrCode.RIGHT,
                                    IoTIrCode.BACK,
                                    IoTIrCode.HOME,
                                    IoTIrCode.CHANNEL_UP,
                                    IoTIrCode.CHANNEL_DOWN,
                                    IoTIrCode.CHANNEL_LIST,
                                    IoTIrCode.VOL_UP,
                                    IoTIrCode.VOL_DOWN,
                                    IoTIrCode.MENU,
                                    IoTIrCode.MUTE
                                )) {
                                    irList.add(IrCode(i, getIrCodeName(i)))
                                }
                                irCodeAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    lnFanTv.visibility = View.GONE
                    lnAc.visibility = View.GONE
                }

            }
            spinnerHub.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    binding.lnDeviceType.visibility = View.VISIBLE
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    binding.lnDeviceType.visibility = View.GONE
                }
            }

            /*
            * Get codes that users want to learn
            * */
            btnConfirm.setOnClickListener {
                irCodeToLearn.clear()
                irCodeLearned.clear()
                for (i in irList) {
                    if (i.isChecked == true) {
                        irCodeToLearn.add(i.irId)
                    }
                }
                currentPos = 0
                startLearning()
            }

        }
    }

    /*
    * To learn a code
    * Para 1: Hub device 's UUID
    * Para 2: Code(Int Type)
    * */
    private fun startLearning() {
        learnIrDialog.show()
        btnContinue.visibility = View.GONE
        btnRetry.visibility = View.GONE
        if (!irCodeLearned.contains(irCodeToLearn[currentPos])) {
            if (binding.spinnerType.selectedItem as DeviceLearnIr == DeviceLearnIr.FAN) {
                txtLearnIr.text = "Hướng điều khiển vào thiết bị hồng ngoại và nhấn nút ${
                    getString(getIrCodeName(irCodeToLearn[currentPos]))
                }"
            } else {
                txtLearnIr.text = "Hướng điều khiển vào thiết bị hồng ngoại và nhấn nút ${
                    getString(getIrCodeName(irCodeToLearn[currentPos]))
                }"
            }
            SmartSdk.configIrRemoteDeviceHandler().learnIr(
                (binding.spinnerHub.selectedItem as IoTDevice).uuid,
                irCodeToLearn[currentPos],
                learnIrCallback
            )
        }
    }

    private val learnIrCallback = object : LearnIrCallback {
        override fun onRequestLearnIrStatus(success: Boolean) {

        }

        override fun onIrRawLearned(requestId: Int, irProtocol: IoTIrProtocolInfo?) {
//            ILogR.D("LearnIrFragment", Gson().toJson(irProtocol), irProtocol?.rawData)
            protocolInfo = irProtocol
            if (protocolInfo?.irp == 65535 || protocolInfo?.irp == IoTIrPrtc.UNKNOWN) {
                 ILogR.D("LearnIrFragment", "Điều khiển này có thể không được hỗ trợ!")
                return
            }
            CoroutineScope(Dispatchers.Main).launch {
                if (irCodeToLearn.size > irCodeLearned.size) {
                    btnContinue.visibility = View.VISIBLE
                    if (binding.spinnerType.selectedItem as DeviceLearnIr == DeviceLearnIr.FAN) {
                        txtLearnIr.text =
                            "Học thành công ${getString(getIrCodeName(irCodeToLearn[currentPos]))}"
                    } else {
                        txtLearnIr.text =
                            "Học thành công ${getString(getIrCodeName(irCodeToLearn[currentPos]))}"
                    }
                    irCodeLearned.add(irCodeToLearn[currentPos])
                } else {
                    learnIrDialog.dismiss()
                    setUpRemoteDialog.show()
                }
            }
        }

        override fun onIrProtocolDetected(irProtocol: IoTIrProtocolInfo?) {
            protocolInfo = irProtocol
        }

        override fun onFailure(requestId: Int, errorCode: Int, msg: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                btnRetry.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpDialogLearnIr() {
        txtLearnIr = learnIrDialog.findViewById<TextView>(R.id.txt_announce)
        btnContinue = learnIrDialog.findViewById<AppCompatButton>(R.id.btn_continue)
        btnRetry = learnIrDialog.findViewById<AppCompatButton>(R.id.btn_retry)
        btnRetry.visibility = View.GONE
        btnCancel = learnIrDialog.findViewById<AppCompatButton>(R.id.btn_cancel)
        learnIrDialog.setCanceledOnTouchOutside(false)
        btnCancel.setOnClickListener {
            learnIrDialog.dismiss()
        }
        btnContinue.setOnClickListener {
            if (irCodeToLearn.size > irCodeLearned.size) {
                currentPos++
                startLearning()
            } else {
                learnIrDialog.dismiss()
                setUpRemoteDialog.show()
            }
        }
        btnRetry.setOnClickListener {
            if (irCodeToLearn.size != 0) {
                startLearning()
            }
        }
        val window = learnIrDialog.window ?: return
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setUpDialogSetUpRemote() {
        val btnContinueSetUp = setUpRemoteDialog.findViewById<AppCompatButton>(R.id.btn_continue)
        val groupSpinner = setUpRemoteDialog.findViewById<Spinner>(R.id.spinner_group)
        val manufacturerSpinner = setUpRemoteDialog.findViewById<Spinner>(R.id.spinner_manufacture)
        val edtDeviceName = setUpRemoteDialog.findViewById<EditText>(R.id.edt_device_name)
        groupSpinner.adapter = groupSpinnerAdapter
        /*
        * Get available manufacturers of the device
        * */
        manufacturerSpinnerAdapter = ManufacturerSpinnerAdapter(
            requireContext(),
            getDeviceName.getClassInfo().entries.filter {
                SmartSdk.configIrRemoteDeviceHandler()
                    .getSupportManufacturers((binding.spinnerType.selectedItem as DeviceLearnIr).type)
                    .contains(
                        it.value
                    )
            }.toList()
        )
        manufacturerSpinner.adapter = manufacturerSpinnerAdapter

        /*
        * Add remote of which codes have been learned
        * */
        btnContinueSetUp.setOnClickListener {
            setUpRemoteDialog.dismiss()
            dialogLoading.show()
            SmartSdk.configIrRemoteDeviceHandler().addIrRemoteLearned(
                (binding.spinnerHub.selectedItem as IoTDevice).uuid,
                edtDeviceName.text.toString(),
                (manufacturerSpinner.selectedItem as Map.Entry<String, Int>).value,
                (binding.spinnerType.selectedItem as DeviceLearnIr).type,
                (groupSpinner.selectedItem as IoTGroup).uuid,
                protocolInfo,
                object : RequestCallback<IoTDevice> {
                    override fun onFailure(errorCode: Int, message: String?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogLoading.dismiss()
                            message?.let {
                                showNoti(it)
                            }
                        }
                    }

                    override fun onSuccess(item: IoTDevice?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogLoading.dismiss()
                            showNoti(R.string.add_ir_remote_success)
                        }
                    }
                }
            )
        }
        setUpRemoteDialog.setCanceledOnTouchOutside(true)
        val window = setUpRemoteDialog.window ?: return
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}