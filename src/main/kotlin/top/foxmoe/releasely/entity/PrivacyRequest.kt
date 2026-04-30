package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

/**
 * 隐私请求实体
 * 用于追踪用户提交的数据主体请求（GDPR/个人信息保护法合规）
 */
@TableName("privacy_requests")
data class PrivacyRequest(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,

    var userId: Long? = null,          // 申请人用户ID

    var requestType: String? = null,   // 请求类型: EXPORT, DELETE, RECTIFY

    var status: String? = null,        // 请求状态: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED

    var details: String? = null,       // 补充说明或申请理由

    var requestedAt: LocalDateTime? = null,  // 申请时间

    var processedAt: LocalDateTime? = null,  // 处理时间

    var processedBy: Long? = null,     // 处理人（管理员ID）

    var adminNotes: String? = null      // 管理员备注
)