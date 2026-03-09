package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("activity_record")
data class ActivityRecord(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var type: String? = null, // 枚举: MASTURBATION, PARTNER_SEX, EDGING
    var protection: String? = null,
    var pleasureRating: Int? = null,
    var healthStatus: String? = null,
    var occurredAt: LocalDateTime? = null,
    var isDeleted: Boolean = false,
    var createdAt: LocalDateTime? = null
)

