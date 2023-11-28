package io.bidapp.networks.applovinmax

import android.app.Activity
import android.content.Context
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal interface IApplovinInitializer {
    fun start(listener: BIDNetworkAdapterProtocol, context: Context)
}