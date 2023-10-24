package io.bidapp.demo;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;


public class FullscreenLoadDelegate implements BIDFullscreenLoadDelegate {
    private final WeakReference<Activity> activityWeakReference;

    public FullscreenLoadDelegate(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void didFailToLoad(AdInfo adInfo, Error error) {
        String waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        String descr = error.getLocalizedMessage();
        System.out.println("Bidapp fullscreen " + waterfallId + " didFailToLoadAd: " + (adInfo != null ? adInfo.getNetworkId() : "null") + " " + adInfo + ". ERROR: " + descr);
    }

    @Override
    public Activity activityForLoadAd() {
        return activityWeakReference.get();
    }

    @Override
    public void didLoad(@Nullable AdInfo adInfo) {
        String waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        String isRewardedString = adInfo != null && adInfo.getFormat() != null ? Boolean.toString(adInfo.getFormat().isRewarded()) : "null";
        System.out.println("Bidapp fullscreen " + waterfallId + " didLoadAd: " + (adInfo != null ? adInfo.getNetworkId() : "null") + " " + adInfo + ". IsRewarded: " + isRewardedString);
    }
}
