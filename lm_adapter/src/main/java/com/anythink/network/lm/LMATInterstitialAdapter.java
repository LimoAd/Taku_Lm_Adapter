package com.anythink.network.lm;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adbid.media.AdbidAdInfo;
import com.adbid.media.AdbidError;
import com.adbid.media.AdbidListener;
import com.adbid.media.ad.AdbidInterstitial;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import java.util.Map;

public class LMATInterstitialAdapter extends CustomInterstitialAdapter {

    public static String TAG = LMATInterstitialAdapter.class.getSimpleName();
    AdbidInterstitial mAdbidInterstitial;

    String mAppId;
    String mUnitId;

    boolean isC2SBidding;

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        if (!(context instanceof Activity)) {
            notifyATLoadFail("", "AdbidAdx Interstitial's context must be activity.");
            return;
        }

        AdbidListener listener = new AdbidListener() {
            @Override
            public void onAdLoad(AdbidAdInfo adbidAdInfo) {
                if (isC2SBidding) {
                    if (mBiddingListener != null) {
                        double price = 0;
                        if (adbidAdInfo != null) {
                            price = adbidAdInfo.getPrice();
                        }
                        LMATBiddingNotice biddingNotice = new LMATBiddingNotice(mAdbidInterstitial);
                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdDataLoaded();
                    }
                }
            }

            @Override
            public void onAdLoadFail(String adUnitId, AdbidError adbidError) {
                String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx interstitial load failed.";
                notifyATLoadFail("", msg);
            }

            @Override
            public void onAdDisplayed(AdbidAdInfo adbidAdInfo) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdDisplayedFailed(AdbidAdInfo adbidAdInfo, AdbidError adbidError) {
                if (mImpressListener != null) {
                    String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx interstitial show failed.";
                    mImpressListener.onInterstitialAdVideoError("", msg);
                }
            }

            @Override
            public void onAdHidden(AdbidAdInfo adbidAdInfo) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
                if (mAdbidInterstitial != null) {
                    try {
                        mAdbidInterstitial.destroy();
                    } catch (Throwable ignored) {
                    }
                }
            }

            @Override
            public void onAdClicked(AdbidAdInfo adbidAdInfo) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        };

        try {
            mAdbidInterstitial = new AdbidInterstitial(mUnitId);
            mAdbidInterstitial.setAdListener(listener);
            mAdbidInterstitial.loadAd();
        } catch (Throwable e) {
            notifyATLoadFail("", "AdbidAdx interstitial load failed." + e.getMessage());
        }
    }

    @Override
    public boolean isAdReady() {
        if (mAdbidInterstitial != null) {
            try {
                return mAdbidInterstitial.isReady();
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mAdbidInterstitial != null) {
            try {
                mAdbidInterstitial.showAd();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getNetworkName() {
        return LMATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra) {
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

    private void initRequestParams(Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "slot_id");
    }

    @Override
    public void destory() {
        if (mAdbidInterstitial != null) {
            try {
                mAdbidInterstitial.destroy();
            } catch (Throwable ignored) {
            }
            mAdbidInterstitial = null;
        }
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
    public com.anythink.core.api.ATInitMediation getMediationInitManager() {
        return LMATInitManager.getInstance();
    }

    @Override
    public boolean startBiddingRequest(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {
        isC2SBidding = true;
        loadCustomNetworkAd(context, serverExtra, localExtra);
        return true;
    }

}