package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("medication")
data class Medication(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var name: String? = null,
    var dosage: String? = null,
    var reminderTime: LocalDateTime? = null,
    var lastTaken: LocalDateTime? = null,
    var isActive: Boolean = true,
    var createdAt: LocalDateTime? = null
)