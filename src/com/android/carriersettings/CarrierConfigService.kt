package com.android.carriersettings

import android.os.PersistableBundle
import android.service.carrier.CarrierIdentifier
import android.service.carrier.CarrierService
import android.telephony.CarrierConfigManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.ZoneOffset

class CarrierConfigService : CarrierService() {
    companion object {
        const val TAG = "CarrierConfigService"
    }

    private fun addVersionString(config: PersistableBundle, settings: CarrierSettings) {
        if (settings.version != 0L) {
            var version = "${settings.canonicalName}-${settings.version}"
            if (settings.hasLastUpdate()) {
                version += "\n${settings.lastUpdate.toDateString()}"
            }
            config.putString(CarrierConfigManager.KEY_CARRIER_CONFIG_VERSION_STRING, version)
        }
    }

    private fun Timestamp.toDateString(): String {
        // Google appears to use Timestamps.normalizedTimestamp somehow, but that's
        // in a private function in a host library. Unless we import it from there,
        // there's no way to use it.
        return Instant.ofEpochSecond(seconds, nanos.toLong())
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .toString()
    }

    @Deprecated("Deprecated in Java")
    override fun onLoadConfig(id: CarrierIdentifier?): PersistableBundle? {
        throw UnsupportedOperationException("Not supported")
    }

    override fun onLoadConfig(subscriptionId: Int, id: CarrierIdentifier?): PersistableBundle? {
        Log.i(TAG, "onLoadConfig: $subscriptionId, $id")
        val bundle = PersistableBundle()
        val noSim =
            id == null || id.mcc.isEmpty() && id.mnc.isEmpty() && id.spn?.isEmpty() != false && id.imsi?.isEmpty() != false && id.gid1?.isEmpty() != false

        if (noSim) {
            try {
                (PbConfigLoader.readSettingsFromAssets("no_sim") ?: PbConfigLoader.readConfigFromPb(
                    ExtendedCarrierIdentifier.DEFAULT
                ))?.let {
                    bundle.putAll(it.configs.toBundle())
                    addVersionString(bundle, it)
                }
                return bundle
            } catch (e: Exception) {
                Log.e(TAG, "Unable to read no sim settings!")
                return null
            }
        } else {
            try {
                PbConfigLoader.readConfigFromPb(ExtendedCarrierIdentifier.DEFAULT)?.also {
                    bundle.putAll(it.configs.toBundle())
                    addVersionString(bundle, it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unable to read default settings!")
                e.printStackTrace()
            }
            try {
                val iccId =
                    getSystemService(SubscriptionManager::class.java).getActiveSubscriptionInfo(
                        subscriptionId
                    ).iccId
                PbConfigLoader.readConfigFromPb(ExtendedCarrierIdentifier(id, iccId))?.also {
                    bundle.putAll(it.configs.toBundle())
                    addVersionString(bundle, it)
                }
                return bundle
            } catch (e: Exception) {
                Log.w(TAG, "Unable to read carrier settings!")
                e.printStackTrace()
                return bundle
            }
        }
    }
}