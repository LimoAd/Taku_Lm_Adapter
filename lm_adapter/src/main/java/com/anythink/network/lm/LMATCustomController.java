package com.anythink.network.lm;

import android.content.pm.PackageInfo;

import com.adbid.sdk.AdbidCustomController;
import com.adbid.sdk.AdbidLocation;
import com.anythink.core.api.ATAdConst;

import java.util.List;

/**
 * 隐私策略控制器，参考官方 KSATCustomController 的设计。
 * <p>
 * 宿主 App 可通过
 * {@code LMATInitManager.getInstance().setLMATCustomController(customController)}
 * 注入自定义隐私策略；未注入时 LMATInitManager 会使用默认实现
 * （各权限默认允许，个性化广告跟随 TopOn 全局设置）。
 */
public abstract class LMATCustomController implements AdbidCustomController {

    /**
     * TopOn 全局个性化广告状态，默认允许。
     * 由 LMATInitManager 在 initSDK 时通过 updatePersonAdStatus 同步
     * ATSDK.getPersionalizedAdStatus() 的结果，App 自定义实现可直接复用。
     */
    private int mPersonAdStatus = ATAdConst.PRIVACY.PERSIONALIZED_ALLOW_STATUS;

    /** 供 LMATInitManager 同步 TopOn 的个性化广告状态 */
    public void updatePersonAdStatus(int personAdStatus) {
        mPersonAdStatus = personAdStatus;
    }

    @Override
    public boolean isSupportPersonalized() {
        return mPersonAdStatus != ATAdConst.PRIVACY.PERSIONALIZED_LIMIT_STATUS;
    }

    @Override
    public boolean isCanUsePhoneState() {
        return true;
    }

    @Override
    public boolean isCanUseLocation() {
        return true;
    }

    @Override
    public boolean isCanUseWifiState() {
        return true;
    }

    @Override
    public boolean isCanUseOaid() {
        return true;
    }

    @Override
    public String getDevOaid() {
        return "";
    }

    @Override
    public boolean isCanUseAppList() {
        return false;
    }

    @Override
    public List<PackageInfo> getAppList() {
        return null;
    }

    @Override
    public boolean isCanUseAndroidId() {
        return true;
    }

    @Override
    public String getAndroidId() {
        return "";
    }

    @Override
    public boolean isCanUseMacAddress() {
        return true;
    }

    @Override
    public String getMacAddress() {
        return "";
    }

    @Override
    public boolean isCanUseWriteExternal() {
        return true;
    }

    @Override
    public boolean isCanUseShakeAd() {
        return true;
    }

    @Override
    public boolean isCanUseRecordAudio() {
        return true;
    }

    @Override
    public String getDevImei() {
        return "";
    }

    @Override
    public String[] getDevImeiList() {
        return null;
    }

    @Override
    public AdbidLocation getLocation() {
        return null;
    }

    @Override
    public boolean isCanUseIP() {
        return true;
    }

    @Override
    public String getIP() {
        return "";
    }
}
