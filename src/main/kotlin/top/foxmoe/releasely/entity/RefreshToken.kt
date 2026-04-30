package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("refresh_token")
data class RefreshToken(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var tokenHash: String? = null,
    var expiresAt: LocalDateTime? = null,
    var revoked: Boolean? = false,
    var createdAt: LocalDateTime? = null
)
