package io.bidapp.networks.applovin

import android.app.Activity
import android.content.Context
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal interface IApplovinInitializer {
    fun start(listener: BIDNetworkAdapterProtocol, context: Context)
}