package io.bidapp.demo;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;


public class FullscreenShowDelegate implements BIDRewardedDelegate, BIDInterstitialDelegate {
    private String sessionId = "";
    private String waterfallId = "";
    private final WeakReference<Activity> activityWeakReference;

    public FullscreenShowDelegate(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void didDisplayAd(AdInfo adInfo) {
        sessionId = adInfo != null ? adInfo.getShowSessionId() : "";
        assert sessionId != null;
        sessionId = sessionId.substring(0, Math.min(3, sessionId.length()));
        waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        System.out.println("Bidapp fullscreen " + sessionId + " " + waterfallId + " didDisplayAd: " + (adInfo != null ? adInfo.getNetworkId() : "null"));
    }

    @Override
    public void didClickAd(AdInfo adInfo) {
        System.out.println("Bidapp fullscreen " + sessionId + " " + waterfallId + " didClickAd: " + (adInfo != null ? adInfo.getNetworkId() : "null"));
    }

    @Override
    public void didHideAd(AdInfo adInfo) {
        System.out.println("Bidapp fullscreen " + sessionId + " " + waterfallId + " didHideAd: " + (adInfo != null ? adInfo.getNetworkId() : "null"));
    }

    @Override
    public void didFailToDisplayAd(AdInfo adInfo, @NonNull Error error) {
        sessionId = adInfo != null ? adInfo.getShowSessionId() : "";
        assert sessionId != null;
        sessionId = sessionId.substring(0, Math.min(3, sessionId.length()));
        waterfallId = adInfo != null ? adInfo.getWaterfallId() : "";
        System.out.println("Bidapp fullscreen " + sessionId + " " + waterfallId + " didFailToDisplayAd " + (adInfo != null ? adInfo.getNetworkId() : "null" + "Error:" + error.getLocalizedMessage()));
    }

    @Override
    public void allNetworksDidFailToDisplayAd() {
        System.out.println("Bidapp fullscreen " + sessionId + " " + waterfallId + " allNetworksDidFailToDisplayAd");
    }

    @Override
    public void didRewardUser() {
        System.out.println("Bidapp fullscreen didRewardUser");
    }

    @Override
    public Activity activityForShowAd() {
        return activityWeakReference.get();
    }


}