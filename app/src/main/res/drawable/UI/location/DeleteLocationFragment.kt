package com.example.rogosample.UI.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.LocationSpinnerAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentDeleteLocationBinding
import rogo.iot.module.platform.callback.RequestCallback
import rogo.iot.module.rogocore.sdk.SmartSdk
import rogo.iot.module.rogocore.sdk.entity.IoTLocation

class DeleteLocationFragment : BaseFragment<FragmentDeleteLocationBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_delete_location

    private val locationSpinnerAdapter by lazy {
        LocationSpinnerAdapter(requireContext(), SmartSdk.locationHandler().all.toMutableList())
    }

    override fun initVariable() {
        super.initVariable()
        binding.apply {
            toolbar.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            toolbar.txtTitle.text = resources.getString(R.string.delete_location)
        }
    }

    override fun initAction() {
        super.initAction()
        binding.apply {
            spinnerLocation.adapter = locationSpinnerAdapter
            /*
            * To delete the location, pass the uuid of the Location
            * */
            btnDeleteLocation.setOnClickListener {
                val uuid = (spinnerLocation.selectedItem as IoTLocation).uuid
                if(uuid.isNullOrEmpty()) {
                    showNoti("Choose location to delete")
                } else {
                    SmartSdk.locationHandler().delete(uuid, object: RequestCallback<Boolean> {
                        override fun onSuccess(p0: Boolean?) {
                            showNoti("Delete success")
                        }

                        override fun onFailure(p0: Int, p1: String?) {
                            p1?.let {
                                showNoti(it)
                            }
                        }

                    })
                }
            }
        }
    }
}