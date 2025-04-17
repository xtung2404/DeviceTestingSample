package com.example.rogosample.UI.authen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.rogosample.R
import com.example.rogosample.adapter.FunctionAdapter
import com.example.rogosample.base.BaseFragment
import com.example.rogosample.databinding.FragmentFunctionBinding
import com.example.rogosample.`object`.Function
import rogo.iot.module.rogocore.sdk.SmartSdk


class FunctionFragment : BaseFragment<FragmentFunctionBinding>() {

    override val layoutId: Int
        get() = R.layout.fragment_function

    private val functionAdapter by lazy {
        FunctionAdapter(onItemClick = {
            when(it) {
                Function.SIGNIN -> {
                    findNavController().navigate(R.id.signInFragment)
                }
                Function.SIGNUP -> {
                    findNavController().navigate(R.id.signUpFragment)
                }
                Function.FORGOTPASSWORD -> {
                    findNavController().navigate(R.id.forgotPasswordFragment)
                }
                else -> {
                    findNavController().navigate(R.id.signOutFragment)
                }
            }
        })
    }

    override fun initVariable() {
        super.initVariable()
        binding.rvFunction.adapter = functionAdapter
        functionAdapter.submitList(Function.getAuthenFuncs())
        initializeBluetoothOrRequestPermission()
    }

    private fun initializeBluetoothOrRequestPermission() {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                requireActivity(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            initializeBluetooth()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, 2)
        }
    }

    private fun initializeBluetooth() {
        val bluetoothManager: BluetoothManager = ContextCompat.getSystemService(
            requireActivity(),
            BluetoothManager::class.java
        ) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(requireActivity(), turnOn, 0, null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            2 -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    initializeBluetooth()
                } else {
                    Log.d("tung", "still fail")
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}