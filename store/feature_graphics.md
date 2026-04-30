# 应用商店素材清单 (Store Assets Checklist)

## 应用图标 (App Icon)

| 平台 | 尺寸要求 | 文件格式 |
|------|----------|----------|
| Google Play | 1024 x 1024 PNG | PNG (无透明背景) |
| App Store | 1024 x 1024 PNG | PNG (无透明背景) |

**设计要求：**
- 清晰展示应用品牌
- 避免过度复杂的细节
- 确保在不同尺寸下仍可识别
- 主色与品牌一致

---

## 应用截图 (Screenshots)

### 手机截图 (Phone Screenshots)

| 平台 | 尺寸要求 | 数量要求 |
|------|----------|----------|
| Google Play | 1080 x 1920 PNG/JPG | 最少2张，建议6-8张 |
| App Store | 6.7" 截图: 1290 x 2796<br>6.5" 截图: 1284 x 2778<br>5.5" 截图: 1242 x 2208 | 各尺寸至少1张 |

**推荐截图内容：**
1. 首页/仪表盘
2. 记录打卡界面
3. 周期日历视图
4. 健康报告/数据洞察
5. 伴侣互动界面
6. 设置/隐私安全界面

### 平板截图 (Tablet Screenshots)

| 尺寸 | 尺寸要求 | 数量要求 |
|------|----------|----------|
| 9-inch 平板 | 2048 x 2732 PNG | 建议1-2张 |
| 12-inch 平板 | 2048 x 2732 PNG | 建议1-2张 |

---

## 功能图形 (Feature Graphic)

| 平台 | 尺寸要求 | 文件格式 |
|------|----------|----------|
| Google Play | 1140 x 624 PNG/JPG | PNG/JPG |
| App Store (Promo) | 可选: 1600 x 900 | PNG/JPG |

**设计要求：**
- 简洁有力，展示核心价值
- 可包含应用名称和 tagline
- 避免文字过多
- 确保在浅色/深色背景下都清晰可见

---

## 隐私政策 URL (Privacy Policy URL)

| 平台 | 要求 |
|------|------|
| Google Play | 必须提供有效的隐私政策页面URL |
| App Store | 必须提供有效的隐私政策页面URL |

**要求：**
- 隐私政策页面必须托管在可公开访问的HTTPS网站
- 页面内容必须包含完整的隐私政策文本
- 建议托管在独立域名或项目官网

---

## 其他可选素材

| 素材类型 | 尺寸要求 | 说明 |
|----------|----------|------|
| 短描述 (Short Description) | 英文80字符内<br>中文30字内 | 用于搜索结果和推荐位 |
| 关键词 (Keywords) | App Store专有 | 最多100字符 |
| 宣传语 (Promo Text) | 最多170字符 | App Store专用 |
| 更新说明 (What's New) | 建议300-500字符 | 版本更新描述 |

---

## 素材文件命名建议

```
store/
├── icons/
│   └── app_icon_1024x1024.png
├── screenshots/
│   ├── phone_01_dashboard.png
│   ├── phone_02_record.png
│   ├── phone_03_calendar.png
│   ├── phone_04_report.png
│   ├── phone_05_partner.png
│   └── phone_06_settings.png
├── tablets/
│   ├── tablet_9inch_screenshot.png
│   └── tablet_12inch_screenshot.png
└── feature_graphic/
    └── feature_graphic_1140x624.png
```