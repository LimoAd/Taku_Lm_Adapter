package com.anythink.network.lm;

import android.util.Log;

import com.adbid.media.AdBidLossInfo;
import com.adbid.media.AdBidPlatform;
import com.adbid.media.ad.AdbidAppOpen;
import com.adbid.media.ad.AdbidBannerView;
import com.adbid.media.ad.AdbidInterstitial;
import com.adbid.media.ad.AdbidNativeLoader;
import com.adbid.media.ad.AdbidRewarded;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingNotice;
import com.anythink.core.api.ATSDK;

import java.util.Map;

public class LMATBiddingNotice implements ATBiddingNotice {

    Object adObject;

    protected LMATBiddingNotice(Object adObject) {
        this.adObject = adObject;
    }

    @Override
    public void notifyBidWin(double costPrice, double secondPrice, Map<String, Object> extra) {
        //RMB cents
        if (ATSDK.isNetworkLogDebug()) {
            Log.i("LMATBiddingNotice", (adObject != null ? adObject.toString() : "") + ": notifyBidWin: " + costPrice);
        }
        notifyWin(costPrice);
    }

    private void notifyWin(double price) {
        try {
            if (adObject instanceof AdbidRewarded) {
                ((AdbidRewarded) adObject).winNotice(price);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidInterstitial) {
                ((AdbidInterstitial) adObject).winNotice(price);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidAppOpen) {
                ((AdbidAppOpen) adObject).winNotice(price);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidBannerView) {
                ((AdbidBannerView) adObject).winNotice(price);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidNativeLoader) {
                ((AdbidNativeLoader) adObject).winNotice(price);
                return;
            }
        } catch (Throwable ignored) {
        }
        adObject = null;
    }

    @Override
    public void notifyBidLoss(String lossCode, double winPrice, Map<String, Object> extra) {
        //RMB cents
        if (ATSDK.isNetworkLogDebug()) {
            Log.i("LMATBiddingNotice", (adObject != null ? adObject.toString() : "") + ": notifyBidLoss lossCode:" + lossCode + ",winPrice:" + winPrice);
        }
        notifyLoss(lossCode, winPrice);
    }

    private void notifyLoss(String lossCode, double winPrice) {
        AdBidLossInfo lossInfo = new AdBidLossInfo(AdBidPlatform.TaKu, winPrice, lossCode);
        try {
            if (adObject instanceof AdbidRewarded) {
                ((AdbidRewarded) adObject).lossNotice(lossInfo);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidInterstitial) {
                ((AdbidInterstitial) adObject).lossNotice(lossInfo);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidAppOpen) {
                ((AdbidAppOpen) adObject).lossNotice(lossInfo);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidBannerView) {
                ((AdbidBannerView) adObject).lossNotice(lossInfo);
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            if (adObject instanceof AdbidNativeLoader) {
                ((AdbidNativeLoader) adObject).lossNotice(lossInfo);
                return;
            }
        } catch (Throwable ignored) {
        }
        adObject = null;
    }

    @Override
    public void notifyBidDisplay(boolean isWinner, double displayPrice) {

    }

    @Override
    public ATAdConst.CURRENCY getNoticePriceCurrency() {
        return ATAdConst.CURRENCY.RMB_CENT;
    }

}