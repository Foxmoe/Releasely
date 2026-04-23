package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("partner")
data class Partner(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var partnerId: Long? = null,
    var status: String? = null,
    var sharedPermissions: String? = null,
    var createdAt: LocalDateTime? = null
)