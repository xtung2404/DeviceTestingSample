package com.example.devicetestingsample.UI.localdb

import android.content.Context
import android.content.SharedPreferences

class PrefsManager (context: Context) {
    companion object {
        private const val PREF_NAME = "MyPrefs"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun setString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String = sharedPreferences.getString(key, "") ?: ""

    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean = sharedPreferences.getBoolean(key, false)


    // Xóa tất cả
    fun clear() {
        editor.clear().apply()
    }
}