package com.anythink.network.lm;

import android.content.Context;
import android.text.TextUtils;

import com.adbid.media.AdbidError;
import com.adbid.media.ad.AdbidNativeLoader;
import com.adbid.media.nativeAd.AdbidNativeAd;
import com.adbid.media.nativeOverseas.NativeAdbidLoadListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

public class LMATAdapter extends CustomNativeAdapter {

    String mAppId;
    String mUnitId;
    int mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;
    boolean isC2SBidding = false;
    AdbidNativeLoader mNativeLoader;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        initRequestParams(serverExtra, localExtra);

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mUnitId)) {
            notifyATLoadFail("", "AdbidAdx appid or unitId is empty.");
            return;
        }

        LMATInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, serverExtra);
            }

            @Override
            public void onFail(String errorMsg) {
                notifyATLoadFail("", errorMsg);
            }
        });
    }

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        try {
            NativeAdbidLoadListener loadListener = new NativeAdbidLoadListener() {
                @Override
                public void onNativeAdLoaded(AdbidNativeAd adbidNativeAd) {
                    if (adbidNativeAd == null) {
                        notifyATLoadFail("", "AdbidAdx native ad is null.");
                        return;
                    }
                    final LMATNativeAd lmatNativeAd = new LMATNativeAd(context, adbidNativeAd, mVideoMuted);

                    if (isC2SBidding) {
                        if (mBiddingListener != null) {
                            double price = 0;
                            if (adbidNativeAd.getAdbidAdInfo() != null) {
                                price = adbidNativeAd.getAdbidAdInfo().getPrice();
                            }
                            LMATBiddingNotice biddingNotice = new LMATBiddingNotice(mNativeLoader);
                            mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), lmatNativeAd);
                        }
                        return;
                    }

                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(new CustomNativeAd[]{lmatNativeAd});
                    }
                }

                @Override
                public void onNativeAdLoadFail(AdbidError adbidError) {
                    String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx native load failed.";
                    notifyATLoadFail("", msg);
                }
            };

            AdbidNativeLoader nativeLoader = new AdbidNativeLoader(context.getApplicationContext(), mUnitId, loadListener);
            mNativeLoader = nativeLoader;
            nativeLoader.loadAd();
        } catch (Throwable e) {
            notifyATLoadFail("", e.getMessage());
        }
    }

    void initRequestParams(Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "slot_id");

        int isVideoMuted = ATInitMediation.getIntFromMap(serverExtra, "video_muted", 0);
        int isVideoAutoPlay = ATInitMediation.getIntFromMap(serverExtra, "video_autoplay", 1);
        int videoDuration = ATInitMediation.getIntFromMap(serverExtra, "video_duration", -1);

        mVideoMuted = isVideoMuted;
        mVideoAutoPlay = isVideoAutoPlay;
        mVideoDuration = videoDuration;
    }

    @Override
    public String getNetworkName() {
        return LMATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return LMATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public int baseOnAdapterBridgeVersion() {
        return LMATInitManager.getInstance().getAdapterBridgeVersion();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return LMATInitManager.getInstance();
    }

    @Override
    public boolean startBiddingRequest(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {
        isC2SBidding = true;
        loadCustomNetworkAd(context, serverExtra, localExtra);
        return true;
    }
}