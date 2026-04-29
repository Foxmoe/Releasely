package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 知识库文章数据
 */
data class KnowledgeArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: KnowledgeCategory,
    val icon: ImageVector,
    val color: Color
)

enum class KnowledgeCategory {
    Safety, Health, Psychology, Relationship
}

val knowledgeArticles = listOf(
    KnowledgeArticle(
        id = "safe-sex",
        title = "安全性行为指南",
        summary = "了解如何保护自己和伴侣的健康",
        content = """
            ## 什么是安全性行为？

            安全性行为是指在性行为过程中采取必要的保护措施，降低性传播感染（STI）和意外怀孕的风险。

            ## 主要保护措施

            ### 1. 使用安全套
            - 每次性行为都应使用新的安全套
            - 检查有效期和包装完整性
            - 正确使用：排出前端空气，全程佩戴
            - 水性或硅基润滑剂可减少破裂风险

            ### 2. 定期检测
            - 有多个性伴侣建议每3-6个月检测一次
            - 常见检测项目：HIV、梅毒、淋病、衣原体
            - 检测是负责任的表现，不是羞耻的事

            ### 3. 暴露前预防（PrEP）
            - 适用于HIV高风险人群
            - 需每日服用，并在医生指导下使用
            - 不能预防其他性传播疾病

            ## 重要提醒
            - 安全套是最有效的双重保护方式（防感染+避孕）
            - 口服避孕药不能预防性传播疾病
            - 事后紧急避孕药不能作为常规避孕手段
        """.trimIndent(),
        category = KnowledgeCategory.Safety,
        icon = Icons.Filled.Notifications,
        color = Color(0xFF4CAF50)
    ),
    KnowledgeArticle(
        id = "contraception",
        title = "避孕方法全解析",
        summary = "了解各种避孕方式的优缺点",
        content = """
            ## 常见避孕方法

            ### 屏障法
            - **男用安全套**：有效率约85%（典型使用），无副作用，可预防STI
            - **女用安全套**：有效率约79%，需提前放置

            ### 激素法
            - **短效口服避孕药**：有效率约91%，需每天定时服用
            - **避孕贴片**：每周更换一次
            - **避孕环**：每月更换或每3周取出1周
            - **避孕针**：每1-3个月注射一次
            - **皮下埋植**：有效期3-5年

            ### 宫内节育器（IUD）
            - **含铜IUD**：有效期10年，无激素
            - **含激素IUD**：有效期3-7年，可减少月经量

            ### 自然法
            - **安全期计算**：需精确追踪周期，失败率较高
            - **体外射精**：失败率高，不推荐作为主要手段

            ## 选择建议
            - 没有"最好"的避孕方式，只有"最适合"的方式
            - 考虑因素：有效性、便利性、副作用、 reversibility
            - 建议咨询专业医生获取个性化建议
        """.trimIndent(),
        category = KnowledgeCategory.Health,
        icon = Icons.Filled.Favorite,
        color = Color(0xFFE91E63)
    ),
    KnowledgeArticle(
        id = "sti-prevention",
        title = "性传播感染预防",
        summary = "认识常见STI及其预防方法",
        content = """
            ## 常见性传播感染（STI）

            ### 细菌感染（可治愈）
            - **淋病**：尿道/宫颈分泌物异常，排尿疼痛
            - **衣原体**：常无症状，可导致不孕
            - **梅毒**：分三期，早期表现为无痛溃疡

            ### 病毒感染（可控制）
            - **HIV**：攻击免疫系统，需终身服药控制
            - **生殖器疱疹**：反复发作，无法根治
            - **HPV**：部分型别可导致宫颈癌，可接种疫苗预防
            - **乙肝**：可通过疫苗预防

            ### 寄生虫感染
            - **滴虫病**：阴道分泌物异常，有异味
            - **阴虱**：瘙痒，可见虫卵

            ## 预防要点
            1. 坚持使用安全套
            2. 定期进行STI筛查
            3. 减少性伴侣数量
            4. 避免在酒精/药物影响下发生性行为
            5. HPV疫苗建议9-45岁接种

            ## 何时就医？
            - 生殖器出现溃疡、水疱或异常分泌物
            - 排尿疼痛或性交疼痛
            - 腹股沟淋巴结肿大
            - 下腹部疼痛（女性）
        """.trimIndent(),
        category = KnowledgeCategory.Health,
        icon = Icons.Filled.Lock,
        color = Color(0xFFF44336)
    ),
    KnowledgeArticle(
        id = "communication",
        title = "亲密关系沟通技巧",
        summary = "学会与伴侣坦诚交流性健康话题",
        content = """
            ## 为什么沟通很重要？

            良好的沟通是健康亲密关系的基石。关于性健康、偏好和界限的坦诚交流可以：
            - 增进双方信任和亲密感
            - 确保双方意愿得到尊重
            - 共同承担健康责任

            ## 实用沟通技巧

            ### 1. 使用"我"语句
            - ❌ "你应该戴套"
            - ✅ "我想保护我们双方的健康，希望我们可以一起使用保护措施"

            ### 2. 选择合适时机
            - 不要在性行为过程中或事后立即讨论敏感话题
            - 选择一个双方放松、没有压力的环境
            - 可以约在散步或喝咖啡时聊

            ### 3. 讨论清单
            - 最近一次STI检测是什么时候？
            - 目前的避孕方式是什么？
            - 对保护措施的态度和偏好
            - 彼此的性健康历史（在自愿分享的前提下）

            ### 4. 尊重界限
            - 对方有权选择不回答某些问题
            - 不要施压或评判
            - 如果沟通困难，可以一起咨询专业医生

            ## 关于知情同意
            - 同意必须是自愿、明确、清醒的
            - 同意可以随时撤回
            - 沉默不等于同意
            - 醉酒/药物影响下的同意无效
        """.trimIndent(),
        category = KnowledgeCategory.Relationship,
        icon = Icons.Filled.Person,
        color = Color(0xFF9C27B0)
    ),
    KnowledgeArticle(
        id = "regular-checkup",
        title = "性健康定期检查",
        summary = "了解何时、何地、如何进行检查",
        content = """
            ## 为什么需要定期检查？

            许多性传播感染在早期没有明显症状，定期检查是发现和治疗的最好方式。

            ## 检查频率建议

            | 人群 | 建议频率 |
            |------|----------|
            | 单一固定伴侣，双方均阴性 | 每年1次 |
            | 有多个性伴侣 | 每3-6个月 |
            | 无保护性行为后 | 2周后、3个月后各1次 |
            | 出现症状时 | 立即就医 |

            ## 常见检查项目

            ### 基础套餐（推荐所有人）
            - HIV抗体检测
            - 梅毒血清学检测
            - 淋病核酸检测
            - 衣原体核酸检测

            ### 扩展套餐（有症状或高风险）
            - 生殖器疱疹病毒检测
            - HPV分型检测
            - 乙肝/丙肝检测
            - 阴道/尿道分泌物常规

            ## 去哪里检查？
            - **公立医院皮肤科/泌尿科/妇科**：价格透明，医保可报销
            - **疾控中心（CDC）**：HIV检测免费且保密
            - **私立体检机构**：服务更好，但费用较高
            - **社区健康服务中心**：方便快捷

            ## 窗口期说明
            - HIV：暴露后2-6周可检测
            - 梅毒：暴露后4-6周可检测
            - 淋病/衣原体：暴露后1-2周可检测
            - 若在高危行为后检测阴性，建议3个月后复查
        """.trimIndent(),
        category = KnowledgeCategory.Health,
        icon = Icons.Filled.Info,
        color = Color(0xFF2196F3)
    )
)

@Composable
fun KnowledgeScreen(onBack: () -> Unit) {
    var selectedArticle by remember { mutableStateOf<KnowledgeArticle?>(null) }

    if (selectedArticle != null) {
        ArticleDetailScreen(
            article = selectedArticle!!,
            onBack = { selectedArticle = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "知识库",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(knowledgeArticles.size) { index ->
                val article = knowledgeArticles[index]
                ArticleCard(article = article, onClick = { selectedArticle = article })
            }
        }
    }
}

@Composable
private fun ArticleCard(article: KnowledgeArticle, onClick: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = article.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.summary,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ArticleDetailScreen(article: KnowledgeArticle, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = article.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            item {
                Text(
                    text = article.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}
