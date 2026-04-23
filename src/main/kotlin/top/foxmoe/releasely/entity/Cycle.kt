package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDate

@TableName("cycle")
data class Cycle(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var startDate: LocalDate? = null,
    var duration: Int? = null,
    var predictedNext: LocalDate? = null,
    var isDeleted: Boolean = false,
    var createdAt: java.time.LocalDateTime? = null
)