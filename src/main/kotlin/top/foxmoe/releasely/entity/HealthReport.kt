package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("health_report")
data class HealthReport(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var period: String? = null,
    var frequencyData: String? = null,
    var protectionRate: Double? = null,
    var createdAt: LocalDateTime? = null
)