# 领摩适配器接入文档

## 1、配置路径

| 广告类型 | 适配器类 |
| --- | --- |
| 开屏 | `com.anythink.network.lm.LMATSplashAdapter` |
| 激励 | `com.anythink.network.lm.LMATRewardedVideoAdapter` |
| 插屏 | `com.anythink.network.lm.LMATInterstitialAdapter` |
| 自渲染信息流 | `com.anythink.network.lm.LMATAdapter` |
| 横幅 | `com.anythink.network.lm.LMATBannerAdapter` |

## 2、初始化入参

### 2.1 初始化配置参数（应用维度）

- `app_id`：应用 ID

### 2.2 广告位入参

放入 Taku 的代码位 ID，无需配置广告源维度参数。

## 3、测试广告位

### 初始化变量

| 初始化变量 | 初始化 ID |
| --- | --- |
| AppID | `10005` |

### 广告 ID

| 广告类型 | 广告 ID |
| --- | --- |
| 插屏广告 | `MTc1MzkzMDgyNTk4MA==` |
| 信息流广告 | `MTc1MzkzMTExNjA4NA==` |
| 激励广告 | `MTc1ODcwMDkyNjk1NA==` |
| 开屏广告 | `MTc1MzkzMDY5NDkyOA==` |
| 横幅广告 | `MTc1ODc5NjM5NTY4OA==` |