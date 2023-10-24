package io.bidapp.networks.applovinmax

import android.app.Activity
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal interface IApplovinInitializer {
    fun start(listener: BIDNetworkAdapterProtocol, activity: Activity)
}