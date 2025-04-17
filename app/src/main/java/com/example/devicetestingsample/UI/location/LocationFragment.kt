package com.example.devicetestingsample.UI.location

import android.view.View
import com.example.devicetestingsample.R
import com.example.devicetestingsample.UI.adapter.LocationAdapter
import com.example.devicetestingsample.UI.base.BaseFragment
import com.example.devicetestingsample.databinding.FragmentLocationBinding
import rogo.iot.module.rogocore.sdk.SmartSdk

class LocationFragment : BaseFragment<FragmentLocationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_location


    private val locationAdapter by lazy {
        LocationAdapter(onItemClick = {
            SmartSdk.setAppLocation(it.uuid)

        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {

        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            rvLocation.adapter = locationAdapter
            val locationList = SmartSdk.locationHandler().all.toMutableList()
            if(locationList.isNullOrEmpty()) {
                rvLocation.visibility = View.GONE
                lnEmpty.visibility =  View.VISIBLE
            } else {
                rvLocation.visibility = View.VISIBLE
                lnEmpty.visibility =  View.GONE
                locationAdapter.submitList(locationList)
            }
        }
    }
}