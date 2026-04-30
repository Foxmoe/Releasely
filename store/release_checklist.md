# 应用商店上架前检查清单 (Release Checklist)

## 一、构建与签名

- [ ] 确认 debug 构建测试通过
- [ ] 确认 release 构建成功生成 APK/AAB
- [ ] 验证签名配置正确（ keystore 路径、别名、密码）
- [ ] 验证 versionCode 和 versionName 正确
- [ ] 确认构建类型为 release（非 debug）
- [ ] 检查 ProGuard/R8 混淆配置（如适用）

## 二、应用信息

- [ ] 应用名称已确定（英文、中文）
- [ ] 简短描述已撰写（80字符内/30字内）
- [ ] 完整描述已撰写（500字符内）
- [ ] 应用图标已准备（1024x1024 PNG）
- [ ] 关键词已确定（App Store）

## 三、应用商店素材

- [ ] Google Play 短描述（英文）已撰写
- [ ] Google Play 完整描述（英文）已撰写
- [ ] App Store 描述（中文）已撰写
- [ ] 手机截图已准备（1080x1920，建议6-8张）
- [ ] 平板截图已准备（2048x2732，9-inch/12-inch）
- [ ] Feature Graphic 已准备（1140x624 PNG）
- [ ] 隐私政策页面已托管（ HTTPS URL）
- [ ] 隐私政策 URL 已确认可访问

## 四、内容分级

- [ ] 完成 Google Play 内容分级问卷
- [ ] 完成 App Store 年龄分级选择
- [ ] 确认应用年龄分级：17+（性健康/健康与健身内容）

## 五、隐私与合规

- [ ] 隐私政策文本已撰写（约1000字）
- [ ] 隐私政策 URL 已提交至应用商店
- [ ] 确认应用权限必要性（位置、通知、存储等）
- [ ] 如使用 Firebase，配置 Firebase 隐私政策合规
- [ ] 如收集健康数据，确认符合当地法规（HIPAA/GDPR/个人信息保护法）

## 六、账户与支付

- [ ] Google Play 开发者账户已注册并付费（如适用）
- [ ] App Store Connect 账户已注册（如适用）
- [ ] 开发者资料已完善（名称、地址、联系方式）
- [ ] 收款账户（银行账户/PayPal）已配置
- [ ] 税务信息已提交

## 七、测试与验证

- [ ] 使用内部测试轨道测试构建
- [ ] 在真实设备上测试（不同 Android 版本）
- [ ] 测试 Google Play 自动登录（如适用）
- [ ] 测试应用内购买流程（如适用）
- [ ] 验证所有功能在 release 构建下正常工作
- [ ] 检查应用启动时间和性能

## 八、上架前最终确认

### Google Play
- [ ] 开发者控制台所有必填项已完成
- [ ] AAB 文件已上传
- [ ] 应用内容符合 Google Play 政策
- [ ] 广告ID配置正确（如无广告可声明）
- [ ] 确认目标受众和内容分级
- [ ] 价格和分发国家/地区已设置
- [ ] 点击"发布"前再次预览所有内容

### App Store（如适用）
- [ ] App Store Connect 所有信息已填写
- [ ] 截图已上传至对应设备尺寸
- [ ] 本地化版本已准备（如英文、中文）
- [ ] 应用预览视频已准备（如有）
- [ ] 审核信息（联系信息、账户类型、演示账号）已提供
- [ ] 点击"提交以供审核"前再次预览所有内容

## 九、发布后

- [ ] 监控应用审核状态
- [ ] 准备应对审核反馈的修改
- [ ] 确认应用已上线
- [ ] 监控用户反馈和评分
- [ ] 准备后续版本更新计划

---

## 注意事项

### Google Play 审核时间
- 通常 1-3 天（首次上架可能更久）
- 敏感类别（健康类）可能需要额外审核

### App Store 审核时间
- 通常 24-48 小时
- 首次上架可能需要 7 天以上

### 健康类应用特殊要求
- 可能需要提供额外的隐私说明
- 可能需要说明数据存储位置（本地 vs 云端）
- 如有医疗建议功能，需明确说明非专业医疗建议

---

## 常用链接

- Google Play Console: https://play.google.com/console
- App Store Connect: https://appstoreconnect.apple.com
- Google Play 政策中心: https://play.google.com/about/developer-content-policy/
- Apple 审核指南: https://developer.apple.com/app-store/review/guidelines/