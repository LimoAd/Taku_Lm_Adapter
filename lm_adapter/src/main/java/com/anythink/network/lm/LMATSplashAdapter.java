package com.anythink.network.lm;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.adbid.media.AdbidAdInfo;
import com.adbid.media.AdbidError;
import com.adbid.media.AdbidListener;
import com.adbid.media.ad.AdbidAppOpen;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.Map;

public class LMATSplashAdapter extends CustomSplashAdapter {

    final String TAG = LMATSplashAdapter.class.getSimpleName();
    boolean isC2SBidding = false;
    private String mAppId;
    private String mUnitId;
    private boolean isReady;
    private AdbidAppOpen mAdbidAppOpen;

    private void startLoadAd(final Context context, Map<String, Object> serverExtra) {
        AdbidListener listener = new AdbidListener() {
            @Override
            public void onAdLoad(AdbidAdInfo adbidAdInfo) {
                isReady = true;
                if (isC2SBidding) {
                    if (mBiddingListener != null) {
                        double price = 0;
                        if (adbidAdInfo != null) {
                            price = adbidAdInfo.getPrice();
                        }
                        LMATBiddingNotice biddingNotice = new LMATBiddingNotice(mAdbidAppOpen);
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
                String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx splash load failed.";
                notifyATLoadFail("", msg);
            }

            @Override
            public void onAdDisplayed(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onAdDisplayedFailed(AdbidAdInfo adbidAdInfo, AdbidError adbidError) {
                if (mImpressionListener != null) {
                    String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx splash show fail";
                    mDismissType = ATAdConst.DISMISS_TYPE.SHOWFAILED;
                    mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "", msg));
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdHidden(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdClicked(AdbidAdInfo adbidAdInfo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }
        };

        try {
            mAdbidAppOpen = new AdbidAppOpen(mUnitId);
            mAdbidAppOpen.setAdListener(listener);
            mAdbidAppOpen.loadAd();
        } catch (Throwable e) {
            notifyATLoadFail("", "AdbidAdx splash load failed." + e.getMessage());
        }
    }

    @Override
    public String getNetworkName() {
        return LMATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return isReady;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra) {
        initRequestParams(serverExtra, localExtra);

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mUnitId)) {
            notifyATLoadFail("", "AdbidAdx appid or unitId is empty.");
            return;
        }

        final Context applicationContext = context.getApplicationContext();
        LMATInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
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

        isReady = false;
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (container == null) {
            if (mImpressionListener != null) {
                mDismissType = ATAdConst.DISMISS_TYPE.SHOWFAILED;
                mImpressionListener.onSplashAdShowFail(ErrorCode.getErrorCode(ErrorCode.adShowError, "", "Container is null"));
                mImpressionListener.onSplashAdDismiss();
            }
            return;
        }

        if (isReady && mAdbidAppOpen != null) {
            container.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mAdbidAppOpen != null) {
                            mAdbidAppOpen.showAd(container);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void destory() {
        if (mAdbidAppOpen != null) {
            try {
                mAdbidAppOpen.destroy();
            } catch (Throwable ignored) {
            }
            mAdbidAppOpen = null;
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