package com.example.rogosample.UI.smart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.SmartSceneAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentGetListScenarioBinding
import rogo.iot.module.platform.define.IoTSmartType
import rogo.iot.module.rogocore.sdk.SmartSdk

class GetListScenarioFragment : BaseFragment<FragmentGetListScenarioBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_get_list_scenario

    var type = IoTSmartType.TYPE_SCENE
    private val smartSceneAdapter by lazy {
        SmartSceneAdapter(onItemClick = {
            SmartSdk.smartFeatureHandler().activeSmart(it)
        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            arguments?.let {
                type = it.getInt("smartType")
            }
            when(type) {
                IoTSmartType.TYPE_SCENE -> {
                    toolbar.txtTitle.text = resources.getString(R.string.get_list_smart_scenario)
                }
                IoTSmartType.TYPE_SCHEDULE -> {
                    toolbar.txtTitle.text = resources.getString(R.string.get_list_smart_schedule)
                }
                else -> {
                    toolbar.txtTitle.text = resources.getString(R.string.get_list_smart_automation)
                }
            }
            rvSmart.adapter = smartSceneAdapter
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            btnGetList.setOnClickListener {
                /*
                * Pass SmartType to get SmartList
                * */
                lnSmart.visibility = View.VISIBLE
                val list = SmartSdk.smartFeatureHandler().getSmartByType(type).toList()
                if(list.isNullOrEmpty()) {
                    lnEmpty.visibility = View.VISIBLE
                    rvSmart.visibility = View.GONE
                } else {
                    lnEmpty.visibility = View.GONE
                    rvSmart.visibility = View.VISIBLE
                    smartSceneAdapter.submitList(list)
                }
            }
        }
    }
}