package com.android.carriersettings

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class PbConfigLoader {
    companion object {
        const val TAG = "PbConfigLoader"
        private val cachedCarriers =
            mutableMapOf<ExtendedCarrierIdentifier, CarrierSettings>()
        private val cachedSettingsAssets = mutableMapOf<String, CarrierSettings>()

        private fun openPbFile(name: String): FileInputStream {
            Log.i(TAG, "Opening $name")
            return FileInputStream(
                File(
                    Environment.getProductDirectory(),
                    "etc" + File.separator + "CarrierSettings" + File.separator + name + ".pb"
                )
            )
        }

        fun getVersion(): Pair<Long, Long> {
            val listVersion = CarrierList.parseFrom(openPbFile("carrier_list")).version
            val othersVersion = MultiCarrierSettings.parseFrom(openPbFile("others")).version
            return Pair(listVersion, othersVersion)
        }

        fun readSettingsFromAssets(name: String): CarrierSettings? {
            try {
                return CarrierSettings.parseFrom(openPbFile(name))?.also {
                    cachedSettingsAssets[name] = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun readConfigFromPb(id: ExtendedCarrierIdentifier): CarrierSettings? {
            Log.i(TAG, "readConfigFromPb")
            cachedCarriers[id]?.also {
                Log.i(TAG, "Cache hit!")
                return it
            }
            val carrierList = CarrierList.parseFrom(openPbFile("carrier_list"))
            val canonicalName = carrierList.find(id)
            var settings: CarrierSettings? = null
            if (canonicalName != null) {
                try {
                    settings = CarrierSettings.parseFrom(openPbFile(canonicalName))
                } catch (e: FileNotFoundException) {
                }
            }
            if (settings == null) {
                val multiCarrierSettings = MultiCarrierSettings.parseFrom(openPbFile("others"))
                for (settings in multiCarrierSettings.settingList) {
                    if (settings.canonicalName == canonicalName) {
                        return CarrierSettings.newBuilder()
                            .mergeFrom(settings)
                            .setVersion(multiCarrierSettings.version)
                            .build()
                    }
                }
            }
            if (settings != null) {
                cachedCarriers[id] = settings
            }
            return settings
        }
    }
}