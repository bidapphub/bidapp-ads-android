package io.bidapp.networks.smaato

import android.app.Activity
import android.content.Context
import com.smaato.sdk.core.SmaatoSdk
import com.smaato.sdk.core.ad.AdRequestParams
import com.smaato.sdk.interstitial.EventListener
import com.smaato.sdk.interstitial.Interstitial
import com.smaato.sdk.interstitial.InterstitialAd
import com.smaato.sdk.interstitial.InterstitialError
import com.smaato.sdk.interstitial.InterstitialRequestError
import com.smaato.sdk.ub.UBBid
import com.smaato.sdk.ub.UnifiedBidding.PrebidListener
import com.smaato.sdk.ub.UnifiedBidding.prebidInterstitial
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
internal class BIDSmaatoInterstitial(
    private val adapter: BIDFullscreenAdapterProtocol? = null,
    private val adSpaceId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial Smaato"
    var interstitialAdListener : InterstitialAdListener? = null


    override fun show(activity: Activity?) {
        if (interstitialAdListener?.interstitialAd == null || interstitialAdListener?.interstitialAd?.isAvailableForPresentation != true){
            adapter?.onFailedToDisplay("Error Smaato showing interstitial ads not ready. adSpaceId: ($adSpaceId)")
            return
        }
        if (activity == null){
            adapter?.onFailedToDisplay("Error Smaato showing interstitial activity is null. adSpaceId: ($adSpaceId)")
            return
        }
        interstitialAdListener?.interstitialAd?.showAd(activity)
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        if (adSpaceId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Smaato interstitial adSpaceId is null or empty")
            return
        }
        interstitialAdListener = InterstitialAdListener(TAG, adapter, adSpaceId)
        if (bidAppBid != null){
            val ubBID = (bidAppBid.nativeBid as? UBBid)
            if (ubBID == null){
                adapter?.onAdFailedToLoadWithError("Smaato interstitial ubBid is null or empty")
                return
            }
            val uniqueID = ubBID.metadata[UBBid.MetadataKeys.UNIQUE_ID].toString()
            val adRequestParam = AdRequestParams.builder().setUBUniqueId(uniqueID).build()
            Interstitial.loadAd(adSpaceId, interstitialAdListener!!, adRequestParam)
        }
        else {
            Interstitial.loadAd(adSpaceId, interstitialAdListener!!)
        }
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return interstitialAdListener?.interstitialAd?.isAvailableForPresentation ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        interstitialAdListener = null
    }

    override fun revenue(): Double? {
        return null
    }

    class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adSpaceId: String?
    ) : EventListener {
        var interstitialAd: InterstitialAd? = null
        var isLoadSuccess = false
        override fun onAdLoaded(p0: InterstitialAd) {
            interstitialAd = p0
            BIDLog.d(tag, "Interstitial ad load $adSpaceId")
            adapter?.onAdLoaded()
            isLoadSuccess = true
        }

        override fun onAdFailedToLoad(p0: InterstitialRequestError) {
            BIDLog.d(tag, "Load interstitial ad failed $adSpaceId Error: ${p0.interstitialError.name}")
            adapter?.onAdFailedToLoadWithError(p0.interstitialError.name)
            isLoadSuccess = false
            interstitialAd = null
        }

        override fun onAdError(p0: InterstitialAd, p1: InterstitialError) {
            if (isLoadSuccess){
                BIDLog.d(tag, "Load interstitial ad failed $adSpaceId Error: ${p1.name}")
                adapter?.onFailedToDisplay(p1.name)
            }
            else {
                BIDLog.d(tag, "Interstitial ad display failed $adSpaceId Error: ${p1.name}")
                BIDLog.d(tag, "Load interstitial ad failed $adSpaceId Error: ${p1.name}")
                adapter?.onAdFailedToLoadWithError(p1.name)
                adapter?.onFailedToDisplay(p1.name)
            }
            interstitialAd = null
        }

        override fun onAdOpened(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial ad open $adSpaceId")
            interstitialAd = p0
        }

        override fun onAdClosed(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial ad hide $adSpaceId")
            adapter?.onHide()
            interstitialAd = p0
        }

        override fun onAdClicked(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial ad clicked $adSpaceId")
            adapter?.onClick()
            interstitialAd = p0
        }

        override fun onAdImpression(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial ad impression $adSpaceId")
            adapter?.onDisplay()
            interstitialAd = p0
        }

        override fun onAdTTLExpired(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial ad TTLExpired $adSpaceId")
            interstitialAd = null
        }

    }
    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            adSpaceId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            BIDSmaatoSDK.COPPA?.let { SmaatoSdk.setCoppa(it) }
            val listener = PrebidListener { p0, p1 ->
                if (p1 != null) {
                    bidCompletion.invoke(null, Error(p1.error.name))
                    return@PrebidListener
                }
                val notifyListener = object : BIDNativeBidNotify{
                    override fun winNotify(secPrice: Double?, secBidder: String?) {}

                    override fun loseNotify(
                        firstPrice: Double?,
                        firstBidder: String?,
                        lossReason: Int?
                    ) {}
                }
                if (p0 == null){
                    bidCompletion.invoke(null, Error("Smaatto interstitial bid is null"))
                    return@PrebidListener
                }
                request.requestBidsWithCompletion(BIDNativeBidData(p0,p0.bidPrice ?: 0.0, notifyListener), adSpaceId, appId, accountId, adFormat, BIDSmaatoSDK.testMode, BIDSmaatoSDK.COPPA, bidCompletion)
            }
            if (adSpaceId == null){
                bidCompletion.invoke(null, Error("Smaatto interstitial adSpaceId is null"))
                return
            }
            prebidInterstitial(adSpaceId, listener)
        }
    }

}