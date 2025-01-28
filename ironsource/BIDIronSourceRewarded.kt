package io.bidapp.networks.ironsource

import android.app.Activity
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyRewardedVideoListener
import com.ironsource.mediationsdk.logger.IronSourceError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDIronSourceRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val instanceId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded IronSource"


    override fun revenue(): Double? {
        return null
    }

    override fun load(context: Any) {
        if (checkRegisterListeners()) {
            adapter?.onAdFailedToLoadWithError("IronSource rewarded instance is busy")
            return
        }
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("IronSource rewarded load is failed. Activity is null")
            return
        }
        if (instanceId == null) {
            adapter?.onAdFailedToLoadWithError("IronSource rewarded load is failed. Instance ID is null")
            return
        }
        val rewardedAdListener = RewardedAdListener(TAG, adapter, instanceId)
        putListener(
            instanceId,
            rewardedAdListener
        )
        if (getListener(instanceId) != null) {
            IronSource.setISDemandOnlyRewardedVideoListener(getListener(instanceId))
        } else {
            adapter?.onAdFailedToLoadWithError("IronSource rewarded listener in weak is null")
            return
        }
        IronSource.loadISDemandOnlyRewardedVideo(context, instanceId)
    }

    override fun show(activity: Activity?) {
        if (!IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)) {
            onFailedToDisplay(instanceId, "IronSource rewarded showing is failure")
            return
        }
        IronSource.showISDemandOnlyRewardedVideo(instanceId)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
       return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        removeListener(instanceId)
        IronSource.removeRewardedVideoListener()
    }

    internal class RewardedAdListener(
       private val tag: String,
        val adapter: BIDFullscreenAdapterProtocol?,
       private val instanceId: String?
    ) :
        ISDemandOnlyRewardedVideoListener {
        var isRewardGranted = false
        override fun onRewardedVideoAdLoadSuccess(p0: String?) {
            BIDLog.d(tag, "Rewarded load $instanceId")
            allOnLoad()
        }

        override fun onRewardedVideoAdLoadFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Load rewarded ad failed $instanceId Error: $errorMessage")
            allOnFailedToLoad(errorMessage)
        }

        override fun onRewardedVideoAdOpened(p0: String?) {
            BIDLog.d(tag, "Rewarded ad impression $instanceId")
            onDisplay(instanceId)
        }

        override fun onRewardedVideoAdShowFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Rewarded ad display failed $instanceId Error: $errorMessage")
            onFailedToDisplay(instanceId, errorMessage)
        }

        override fun onRewardedVideoAdClicked(p0: String?) {
            BIDLog.d(tag, "Rewarded ad clicked $instanceId")
            onClickListener(instanceId)
        }

        override fun onRewardedVideoAdRewarded(p0: String?) {
            BIDLog.d(tag, "Ad rewarded $instanceId")
            isRewardGranted = true
        }

        override fun onRewardedVideoAdClosed(p0: String?) {
            BIDLog.d(tag, "Rewarded ad hide. $instanceId")
            if (isRewardGranted) {
                onRewarded(instanceId)
                isRewardGranted = false
            }
            onClose(instanceId)
        }

    }

    companion object {
        private val listenersRewardedMap =
            HashMap<String, WeakReference<RewardedAdListener>>()

        fun checkRegisterListeners(): Boolean {
            return listenersRewardedMap.values.any { it.get() != null }
        }

        fun putListener(instanceId: String?, listener: RewardedAdListener?) {
            if (listener != null && instanceId != null) listenersRewardedMap[instanceId] =
                WeakReference(listener)
        }

        fun getListener(key: String?): RewardedAdListener? {
            return key?.let { listenersRewardedMap[it]?.get() }
                ?: run {
                    BIDLog.e(
                        "Rewarded IronSource",
                        "Listener for key $key is null or has been garbage collected."
                    )
                    null
                }
    }

        fun removeListener(key: String?) {
            key?.let {
                listenersRewardedMap[it]?.clear()
                listenersRewardedMap.remove(it)
            }
        }

        fun allOnFailedToLoad(err: String) {
            listenersRewardedMap.forEach { (key, ref) ->
                ref.get()?.adapter?.onAdFailedToLoadWithError(err)
            }
            listenersRewardedMap.clear()
        }

        fun allOnLoad() {
            listenersRewardedMap.forEach { (key, ref) ->
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

        fun onRewarded(instanceId: String?) {
            getListener(instanceId)?.adapter?.onReward()
        }

        fun onClose(instanceId: String?) {
            getListener(instanceId)?.adapter?.onHide()
            removeListener(instanceId)
        }

    }

}

