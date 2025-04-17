package com.example.rogosample.UI.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.FunctionAdapter
import com.example.rogosample.adapter.LocationAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentLocationBinding
import rogo.iot.module.rogocore.sdk.SmartSdk

class LocationFragment : BaseFragment<FragmentLocationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_location
    /*
    * To set the default location for Application, pass the UUID of the location
    * */
    private val locationAdapter by lazy {
        LocationAdapter(onItemClick = {
            SmartSdk.setAppLocation(it.uuid)
            findNavController().navigate(R.id.generalFunction)
        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            if(findNavController().previousBackStackEntry?.destination?.id == R.id.splashFragment) {
                toolbar.btnBack.visibility = View.INVISIBLE
            }
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            toolbar.txtTitle.text = resources.getString(R.string.get_list_location)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            rvLocation.adapter = locationAdapter
            btnGetList.setOnClickListener {
                lnLocations.visibility = View.VISIBLE
                /*
                * Get all the available locations
                * */
                val locationList = SmartSdk.locationHandler().all.toMutableList()
                if(locationList.isNullOrEmpty()) {
                    rvLocation.visibility = View.GONE
                    lnEmpty.visibility =  View.VISIBLE
                } else {
                    rvLocation.visibility = View.VISIBLE
                    lnEmpty.visibility =  View.GONE
                    locationAdapter.submitList(
                        locationList
                    )
                }
            }
        }
    }
}