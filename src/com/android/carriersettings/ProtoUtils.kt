package com.android.carriersettings

import android.os.PersistableBundle
import android.util.Log
import com.android.carriersettings.CarrierConfig.Config.ValueCase.BOOLEAN_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.BUNDLE_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.DOUBLE_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.INTEGER_ARRAY_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.INTEGER_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.LONG_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.STRING_ARRAY_VALUE
import com.android.carriersettings.CarrierConfig.Config.ValueCase.STRING_VALUE
import com.android.carriersettings.CarrierId.MvnoDataCase.GID
import com.android.carriersettings.CarrierId.MvnoDataCase.ICCID
import com.android.carriersettings.CarrierId.MvnoDataCase.IMSI
import com.android.carriersettings.CarrierId.MvnoDataCase.MVNODATA_NOT_SET
import com.android.carriersettings.CarrierId.MvnoDataCase.SPN

const val TAG = "ProtoUtils"

fun CarrierList.find(id: ExtendedCarrierIdentifier): String? {
    for (carrierMap in entryList) {
        val carrierId = carrierMap.carrierId
        if (id.mcc + id.mnc == carrierId.mccMnc) {
            Log.i(TAG, "Checking carrier ${carrierId.mccMnc}")
            when (carrierId.mvnoDataCase) {
                SPN -> {
                    id.spn?.let {
                        if (it.equals(
                                carrierId.spn, ignoreCase = true
                            )
                        ) {
                            Log.i(TAG, "Carrier ${carrierId.mccMnc} matched!")
                            return carrierMap.canonicalName
                        }
                    }
                }

                IMSI -> {
                    id.imsi?.let {
                        if (it.matches(
                                carrierId.imsi.replace("[xX]*$", "[0-9]*")
                                    .replace("[xX]", "[0-9]")
                                    .toRegex()
                            )
                        ) {
                            Log.i(TAG, "Carrier ${carrierId.mccMnc} matched!")
                            return carrierMap.canonicalName
                        }
                    }
                }

                GID -> {
                    id.gid1?.let {
                        val gid = carrierId.gid
                        if (it.length >= gid.length && gid.equals(
                                it.substring(0, gid.length), ignoreCase = true
                            )
                        ) {
                            Log.i(TAG, "Carrier ${carrierId.mccMnc} matched!")
                            return carrierMap.canonicalName
                        }
                    }
                }

                ICCID -> if (id.iccId.startsWith(carrierId.iccid)) {
                    Log.i(TAG, "Carrier ${carrierId.mccMnc} matched!")
                    return carrierMap.canonicalName
                }

                MVNODATA_NOT_SET -> return carrierMap.canonicalName
            }
        }
    }
    return null
}

fun CarrierConfig.toBundle(): PersistableBundle {
    val bundle = PersistableBundle()
    for (config in configList) {
        Log.i(TAG, "Adding key ${config.key}")
        when (config.valueCase) {
            STRING_VALUE -> bundle.putString(config.key, config.stringValue)
            INTEGER_VALUE -> bundle.putInt(config.key, config.integerValue)
            LONG_VALUE -> bundle.putLong(config.key, config.longValue)
            BOOLEAN_VALUE -> bundle.putBoolean(config.key, config.booleanValue)
            DOUBLE_VALUE -> bundle.putDouble(config.key, config.doubleValue)
            STRING_ARRAY_VALUE -> {
                bundle.putStringArray(
                    config.key, arrayOf<String>() + config.stringArrayValue.itemList
                )
            }

            INTEGER_ARRAY_VALUE -> {
                bundle.putIntArray(config.key, intArrayOf() + config.integerArrayValue.itemList)
            }

            BUNDLE_VALUE -> bundle.putPersistableBundle(config.key, config.bundleValue.toBundle())
            else -> {}
        }
    }
    return bundle
}