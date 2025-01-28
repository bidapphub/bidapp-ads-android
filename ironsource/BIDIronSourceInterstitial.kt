package io.bidapp.networks.ironsource

import android.app.Activity
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyInterstitialListener
import com.ironsource.mediationsdk.logger.IronSourceError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDIronSourceInterstitial(
    private val adapter: BIDFullscreenAdapterProtocol? = null,
    private val instanceId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial IronSource"

    override fun revenue(): Double? {
        return null
    }

    override fun load(context: Any) {
        if (checkRegisterListeners()) {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial instance is busy")
            return
        }
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial load is failed. Activity is null")
            return
        }
        if (instanceId == null) {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial load is failed. Instance ID is null")
            return
        }
        val interstitialAdListener = InterstitialAdListener(TAG, adapter, instanceId)
        putListener(instanceId, interstitialAdListener)

        if (getListener(instanceId) != null) {
            IronSource.setISDemandOnlyInterstitialListener(getListener(instanceId))
        } else {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial listener in weak is null")
            return
        }
        IronSource.loadISDemandOnlyInterstitial(context, instanceId)
    }

    override fun show(activity: Activity?) {
        if (!IronSource.isISDemandOnlyInterstitialReady(instanceId)) {
            onFailedToDisplay(
                instanceId,
                "IronSource interstitial showing is failure is ad not ready"
            )
            return
        }
        IronSource.showISDemandOnlyInterstitial(instanceId)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean { return IronSource.isISDemandOnlyInterstitialReady(instanceId)
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        removeListener(instanceId)
        IronSource.removeInterstitialListener()
    }

    internal class InterstitialAdListener(
        private val tag: String,
        val adapter: BIDFullscreenAdapterProtocol?,
        private val instanceId: String?
    ) :
        ISDemandOnlyInterstitialListener {
        override fun onInterstitialAdReady(p0: String?) {
            BIDLog.d(tag, "Interstitial ad load $instanceId")
            allOnLoad()
        }

        override fun onInterstitialAdLoadFailed(p0: String?, p1: IronSourceError?) {
            IronSource.removeInterstitialListener()
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Load interstitial ad failed $instanceId Error: $errorMessage")
            allOnFailedToLoad(errorMessage)
        }

        override fun onInterstitialAdOpened(p0: String?) {
            BIDLog.d(tag, "Interstitial ad impression $instanceId")
            onDisplay(instanceId)
        }

        override fun onInterstitialAdShowFailed(p0: String?, p1: IronSourceError?) {
            IronSource.removeInterstitialListener()
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Interstitial ad display failed $instanceId Error: $errorMessage")
            onFailedToDisplay(instanceId, errorMessage)
        }

        override fun onInterstitialAdClicked(p0: String?) {
            BIDLog.d(tag, "Interstitial ad clicked $instanceId")
            onClickListener(instanceId)
        }

        override fun onInterstitialAdClosed(p0: String?) {
            IronSource.removeInterstitialListener()
            BIDLog.d(tag, "Interstitial ad hide $instanceId")
            onClose(instanceId)
        }

    }

    companion object {
        private val listenersInterstitialMap =
            HashMap<String, WeakReference<InterstitialAdListener>>()

        fun checkRegisterListeners(): Boolean {
            return listenersInterstitialMap.values.any { it.get() != null }
        }


        fun putListener(instanceId: String?, listener: InterstitialAdListener?) {
            if (listener != null && instanceId != null) listenersInterstitialMap[instanceId] =
                WeakReference(listener)
        }

        fun getListener(key: String?): InterstitialAdListener? {
            return key?.let { listenersInterstitialMap[it]?.get() }
                ?: run {
                    BIDLog.e(
                        "Interstitial IronSource",
                        "Listener for key $key is null or has been garbage collected."
                    )
                    null
                }
        }

        fun removeListener(key: String?) {
            key?.let {
                listenersInterstitialMap[it]?.clear()
                listenersInterstitialMap.remove(it)
            }
        }


        fun allOnFailedToLoad(err: String) {
            listenersInterstitialMap.forEach { (key, ref) ->
                ref.get()?.adapter?.onAdFailedToLoadWithError(err)
            }
            listenersInterstitialMap.clear()
        }


        fun allOnLoad() {
            listenersInterstitialMap.forEach { (key, ref) ->
                ref.get()?.adapter?.onAdLoaded()
            }
        }

        fun onClickListener(instanceId: String?) {
            getListener(instanceId)?.adapter?.onClick()
        }

        fun onFailedToDisplay(key: String?, err: String) {
            getListener(key)?.adapter?.onFailedToDisplay(err)
            removeListener(key)
        }

        fun onDisplay(instanceId: String?) {
            getListener(instanceId)?.adapter?.onDisplay()
        }

        fun onClose(instanceId: String?) {
            getListener(instanceId)?.adapter?.onHide()
            removeListener(instanceId)
        }

    }

}