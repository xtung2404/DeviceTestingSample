package com.example.rogosample.UI.virtualgroup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentControlGroupBinding
import rogo.iot.module.platform.define.IoTAttribute
import rogo.iot.module.platform.define.IoTCmdConst
import rogo.iot.module.platform.define.IoTDeviceType
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.callback.AckStatusCallback
import rogo.iot.module.rogocore.sdk.define.IoTAckStatus
import rogo.iot.module.rogocore.sdk.entity.IoTGroup

class ControlGroupFragment : BaseFragment<FragmentControlGroupBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_control_group

    private lateinit var currentGroup: IoTGroup
    private var light_sat = 0f
    private var light_hue = 0f

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            arguments?.let {
                currentGroup = SmartSdk.groupHandler().get(it.getString("ioTGroup", null))
                SmartSdk.groupHandler().getGroupCtlMembers(currentGroup.uuid).forEach {groupMember ->
                    val device = SmartSdk.deviceHandler().get(groupMember.deviceId)
                    if (device.containtFeature(IoTAttribute.ACT_ONOFF)) {
                        lnOnoff.visibility = View.VISIBLE
                    }
                    if (device.containtFeature(IoTAttribute.ACT_BRIGHTNESS)) {
                        lnBrightness.visibility = View.VISIBLE
                    }
                    if (device.containtFeature(IoTAttribute.ACT_OPEN_CLOSE)) {
                        lnOpenclose.visibility = View.VISIBLE
                    }
                    if (device.containtFeature(IoTAttribute.ACT_LOCK_UNLOCK)) {
                        lnLockUnlock.visibility = View.VISIBLE
                    }
                    if (device.containtFeature(IoTAttribute.ACT_COLOR_HSV)) {
                        lnSaturation.visibility = View.VISIBLE
                    }
                }
            }

        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            switchOnoff.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    SmartSdk.controlHandler().controlGroupPower(
                        currentGroup.uuid,
                        p1,
                        IoTDeviceType.ALL,
                        object : AckStatusCallback(3000) {
                            override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                            }

                        }
                    )
                }
            })

            /*
            * Choose open or close
            * */
            btnOpen.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    currentGroup.uuid,
                    true,
                    IoTCmdConst.OPENCLOSE_MODE_OPEN,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }

                    }
                )
            }

            btnClose.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    currentGroup.uuid,
                    true,
                    IoTCmdConst.OPENCLOSE_MODE_CLOSE,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }

                    }
                )
            }
            btnStop.setOnClickListener {
                SmartSdk.controlHandler().controlMotorCurtain(
                    currentGroup.uuid,
                    true,
                    IoTCmdConst.OPENCLOSE_MODE_STOP,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }

                    }
                )
            }

            /*
            * Choose lock or unlock
            * */
            btnLock.setOnClickListener {
                SmartSdk.controlHandler().controlLock(
                    currentGroup.uuid,
                    IoTCmdConst.DOOR_LOCKED,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }

                    })
            }

            btnUnlock.setOnClickListener {
                SmartSdk.controlHandler().controlLock(
                    currentGroup.uuid,
                    IoTCmdConst.DOOR_UNLOCKED,
                    object : AckStatusCallback(3000) {
                        override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                        }

                    })
            }

            /*
            * Change the color, brightness of the device
            * */
            sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    SmartSdk.controlHandler().controlDim(
                        currentGroup.uuid, true, p1 * 10,
                        object : AckStatusCallback(3000) {
                            override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                            }

                        }
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            sbSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    light_sat = p1.toFloat()
                    SmartSdk.controlHandler().controlLightHsv(
                        currentGroup.uuid,
                        true,
                        floatArrayOf(light_sat, light_hue, 1F),
                        object : AckStatusCallback(3000) {
                            override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                            }

                        }
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            sbColor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    light_hue = p1 * 0.2F
                    SmartSdk.controlHandler().controlLightHsv(
                        currentGroup.uuid,
                        true,
                        floatArrayOf(light_sat, light_hue, 1F),
                        object : AckStatusCallback(3000) {
                            override fun onStatus(p0: Int, p1: IoTAckStatus?, p2: Int) {

                            }

                        }
                    )
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }
    }

}