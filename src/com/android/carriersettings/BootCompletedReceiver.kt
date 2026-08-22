package com.android.carriersettings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.CarrierConfigManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class BootCompletedReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(TAG, "context is null!")
            return
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val savedListVersion = preferences.getLong("list_version", 0)
        val savedOthersVersion = preferences.getLong("others_version", 0)
        val currentVersion = PbConfigLoader.getVersion()
        if (currentVersion.first > savedListVersion || currentVersion.second > savedOthersVersion) {
            Log.i(
                TAG,
                "Carrier settings updated! list: $savedListVersion -> ${currentVersion.first}, " +
                        "others: $savedOthersVersion -> ${currentVersion.second}"
            )
            context.getSystemService(SubscriptionManager::class.java).activeSubscriptionIdList.forEach {
                context.getSystemService(CarrierConfigManager::class.java)
                    .notifyConfigChangedForSubId(it)
            }
            preferences.edit {
                putLong("list_version", currentVersion.first)
                putLong("others_version", currentVersion.second)
            }
        }
    }
}