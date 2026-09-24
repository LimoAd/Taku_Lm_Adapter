package com.anythink.network.lm;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adbid.media.AdbidAdInfo;
import com.adbid.media.AdbidError;
import com.adbid.media.AdbidRewardListener;
import com.adbid.media.ad.AdbidRewarded;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

public class LMATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = LMATRewardedVideoAdapter.class.getSimpleName();
    AdbidRewarded mAdbidRewarded;
    String mAppId;
    String mUnitId;

    private boolean isC2SBidding = false;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        initRequestParams(serverExtra, localExtra);

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mUnitId)) {
            notifyATLoadFail("", "AdbidAdx appId or unitId is empty.");
            return;
        }

        final Context applicationContext = context.getApplicationContext();
        LMATInitManager.getInstance().initSDK(applicationContext, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(applicationContext, serverExtra);
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

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        AdbidRewardListener listener = new AdbidRewardListener() {
            @Override
            public void onUserReward(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onAdLoad(AdbidAdInfo adbidAdInfo) {
                if (isC2SBidding) {
                    if (mBiddingListener != null) {
                        double price = 0;
                        if (adbidAdInfo != null) {
                            price = adbidAdInfo.getPrice();
                        }
                        LMATBiddingNotice biddingNotice = new LMATBiddingNotice(mAdbidRewarded);
                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }
            }

            @Override
            public void onAdLoadFail(String adUnitId, AdbidError adbidError) {
                String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx rewarded video load failed.";
                notifyATLoadFail("", msg);
            }

            @Override
            public void onAdDisplayed(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onAdDisplayedFailed(AdbidAdInfo adbidAdInfo, AdbidError adbidError) {
                if (mImpressionListener != null) {
                    String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx rewarded video show failed.";
                    mImpressionListener.onRewardedVideoAdPlayFailed("", msg);
                }
            }

            @Override
            public void onAdHidden(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClicked(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }
        };

        try {
            mAdbidRewarded = new AdbidRewarded(mUnitId);
            mAdbidRewarded.setAdListener(listener);
            mAdbidRewarded.loadAd();
        } catch (Throwable e) {
            notifyATLoadFail("", "AdbidAdx rewarded video load failed." + e.getMessage());
        }
    }

    @Override
    public String getNetworkName() {
        return LMATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mAdbidRewarded != null) {
            try {
                mAdbidRewarded.destroy();
            } catch (Throwable ignored) {
            }
            mAdbidRewarded = null;
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
    public boolean isAdReady() {
        if (mAdbidRewarded != null) {
            try {
                return mAdbidRewarded.isReady();
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mAdbidRewarded != null) {
            try {
                mAdbidRewarded.showAd();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
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