package com.android.carriersettings

import android.service.carrier.CarrierIdentifier

class ExtendedCarrierIdentifier(
    mcc: String,
    mnc: String,
    spn: String?,
    imsi: String?,
    gid1: String?,
    gid2: String?,
    val iccId: String
) : CarrierIdentifier(mcc, mnc, spn, imsi, gid1, gid2) {
    constructor(id: CarrierIdentifier, iccId: String) : this(
        id.mcc, id.mnc, id.spn, id.imsi, id.gid1, id.gid2, iccId
    )

    override fun toString(): String {
        return "{ExtendedCarrierIdentifier" + "carrierIdentifier=${super.toString()}" + ", iccId=$iccId"
    }

    companion object {
        val DEFAULT = ExtendedCarrierIdentifier("000", "000", null, null, null, null, "")
    }
}