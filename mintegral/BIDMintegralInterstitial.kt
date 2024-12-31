package io.bidapp.networks.mintegral

import android.app.Activity
import android.content.Context
import com.mbridge.msdk.mbbid.out.BidListennning
import com.mbridge.msdk.mbbid.out.BidLossCode
import com.mbridge.msdk.mbbid.out.BidManager
import com.mbridge.msdk.mbbid.out.BidResponsed
import com.mbridge.msdk.newinterstitial.out.MBBidNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.MBNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener
import com.mbridge.msdk.out.MBridgeIds
import com.mbridge.msdk.out.RewardInfo
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDMintegralInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    private val tagId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial Mintegral"
    private var interstitialAdListener: InterstitialAdListener? = null
    private val format = tagId?.split("#", limit = 2)
    val placementId = format?.getOrNull(0)
    val unitId = format?.getOrNull(1)
    var mbBidNewInterstitialHandler: MBBidNewInterstitialHandler? = null
    var mbNewInterstitialHandler: MBNewInterstitialHandler? = null

    override fun load(context: Any, bidAppBid: BidappBid?) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Load Mintegral interstitial failed. Context is null")
            return
        }
        if (placementId == null || unitId == null) {
            adapter?.onAdFailedToLoadWithError("Load Mintegral interstitial failed." + if (placementId == null) "Placement ID is null" else "Unit ID is null")
            return
        }
        interstitialAdListener = InterstitialAdListener(TAG, adapter, placementId)
        if (bidAppBid != null) {
            if (bidAppBid.nativeBid as? String == null) {
                adapter?.onAdFailedToLoadWithError("Load Mintegral bid interstitial failed. Bid ID is null")
                return
            }
            mbBidNewInterstitialHandler =
                MBBidNewInterstitialHandler(context as? Context, placementId, unitId)
            mbBidNewInterstitialHandler?.setInterstitialVideoListener(interstitialAdListener)
            mbBidNewInterstitialHandler?.loadFromBid(bidAppBid.nativeBid as? String)

            return
        }
        mbNewInterstitialHandler = MBNewInterstitialHandler(context, placementId, unitId)
        mbNewInterstitialHandler?.setInterstitialVideoListener(interstitialAdListener)
        mbNewInterstitialHandler?.load()
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun show(activity: Activity?) {
        if (mbBidNewInterstitialHandler != null && mbBidNewInterstitialHandler?.isBidReady == true){
            mbBidNewInterstitialHandler?.showFromBid()
        }
        else if (mbNewInterstitialHandler != null && mbNewInterstitialHandler?.isReady == true){
            mbNewInterstitialHandler?.show()
        }
        else {
            adapter?.onFailedToDisplay("Mintegral interstitial showing is failure is ad not ready $placementId")
        }
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return if (mbBidNewInterstitialHandler != null && mbBidNewInterstitialHandler?.isBidReady == true) true
        else if (mbNewInterstitialHandler != null && mbNewInterstitialHandler?.isReady == true) true
        else false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        if (mbBidNewInterstitialHandler != null){
            mbBidNewInterstitialHandler?.setInterstitialVideoListener(null)
            mbBidNewInterstitialHandler = null
        }
        if (mbNewInterstitialHandler != null){
            mbNewInterstitialHandler?.setInterstitialVideoListener(null)
            mbNewInterstitialHandler = null
        }
        interstitialAdListener = null
    }

    private class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val placementId: String?,
    ) : NewInterstitialListener  {
        override fun onLoadCampaignSuccess(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial load campaign success $placementId")
        }

        override fun onResourceLoadSuccess(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial ad load $placementId")
            adapter?.onAdLoaded()
        }

        override fun onResourceLoadFail(p0: MBridgeIds?, p1: String?) {
            val errorMessage = p1 ?: "Unknown Error"
            BIDLog.d(tag, "Load interstitial ad failed $placementId Error: $errorMessage")
            adapter?.onAdFailedToLoadWithError(errorMessage)
        }

        override fun onAdShow(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial ad impression $placementId")
            adapter?.onDisplay()
        }

        override fun onAdClose(p0: MBridgeIds?, p1: RewardInfo?) {
            BIDLog.d(tag, "Interstitial ad hide $placementId")
            adapter?.onHide()
        }

        override fun onShowFail(p0: MBridgeIds?, p1: String?) {
            val errorMessage = p1 ?: "Unknown Error"
            BIDLog.d(tag, "Interstitial ad display failed $placementId Error: $errorMessage")
            adapter?.onFailedToDisplay(errorMessage)
        }

        override fun onAdClicked(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial ad clicked $placementId")
            adapter?.onClick()
        }

        override fun onVideoComplete(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial ad complete $placementId")
        }

        override fun onAdCloseWithNIReward(p0: MBridgeIds?, p1: RewardInfo?) {
            BIDLog.d(tag, "Interstitial ad with reward close $placementId")
        }

        override fun onEndcardShow(p0: MBridgeIds?) {
            BIDLog.d(tag, "Interstitial ad end card show $placementId")
        }
    }

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            tagId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            val format = tagId?.split("#", limit = 2)
            val placementId = format?.getOrNull(0)
            val unitId = format?.getOrNull(1)
            if (placementId == null) {
                bidCompletion.invoke(null, Error("Mintegral interstitial bid failed placement ID is null"))
                return
            }
            if (unitId == null) {
                bidCompletion.invoke(null, Error("Mintegral interstitial bid failed unit ID is null"))
                return
            }
            val bidManager = BidManager(placementId, unitId)
            bidManager.setBidListener(object : BidListennning {
                override fun onFailed(p0: String?) {
                    bidCompletion.invoke(null, Error("Mintgral bid ${p0 ?: "Unknown error"}"))
                }

                override fun onSuccessed(p0: BidResponsed?) {
                    if (p0 == null) {
                        bidCompletion.invoke(
                            null,
                            Error("Mintegral interstitial bid failed. BidResponse is null ")
                        )
                    } else {
                        val notifyListener = object : BIDNativeBidNotify {
                            override fun winNotify(secPrice: Double?, secBidder: String?) {
                                if (context != null) p0.sendWinNotice(context)
                            }

                            override fun loseNotify(
                                firstPrice: Double?,
                                firstBidder: String?,
                                lossReason: Int?
                            ) {
                                if (context != null) p0.sendLossNotice(
                                    context,
                                    BidLossCode.bidPriceNotHighest()
                                )
                            }
                        }
                        request.requestBidsWithCompletion(
                            BIDNativeBidData(
                                p0.bidToken,
                                p0.price.toDoubleOrNull() ?: 0.0,
                                notifyListener
                            ),
                            tagId,
                            appId,
                            accountId,
                            adFormat,
                            BIDMintegralSDK.testMode,
                            BIDMintegralSDK.COPPA,
                            bidCompletion
                        )
                    }
                }
            })
            bidManager.bid()
        }
    }
}