package com.bidapp.demo;

import androidx.annotation.Nullable;

import io.bidapp.sdk.AdInfo;
import io.bidapp.sdk.BIDFullscreenLoadDelegate;

public class FullscreenLoadDelegate implements BIDFullscreenLoadDelegate {



    @Override
    public void didFailToLoad(AdInfo adInfo, Error error) {
        String waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        String descr = error.getLocalizedMessage();
        System.out.println("Bidapp fullscreen " + waterfallId + " didFailToLoadAd: " + (adInfo != null ? adInfo.getNetworkId() : "null") + " " + adInfo + ". ERROR: " + descr);
    }

    @Override
    public void didLoad(@Nullable AdInfo adInfo) {
        String waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        String isRewardedString = adInfo != null && adInfo.getAdFormat() != null ? Boolean.toString(adInfo.getAdFormat().isRewarded()) : "null";
        System.out.println("Bidapp fullscreen " + waterfallId + " didLoadAd: " + (adInfo != null ? adInfo.getNetworkId() : "null") + " " + adInfo + ". IsRewarded: " + isRewardedString);
    }
}
