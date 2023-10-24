package io.bidapp.networks.applovin

import android.app.Activity
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal interface IApplovinInitializer {
    fun start(listener: BIDNetworkAdapterProtocol, activity: Activity)
}