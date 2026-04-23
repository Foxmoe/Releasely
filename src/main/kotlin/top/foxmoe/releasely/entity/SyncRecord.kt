package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("sync_record")
data class SyncRecord(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var entityType: String? = null, // activity, cycle, medication, partner, health_report
    var entityId: Long? = null,
    var action: String? = null, // CREATE, UPDATE, DELETE
    var localTimestamp: LocalDateTime? = null,
    var serverTimestamp: LocalDateTime? = null,
    var syncStatus: String? = null, // PENDING, SYNCED, CONFLICT
    var payload: String? = null, // JSON payload for conflict resolution
    var createdAt: LocalDateTime? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SYNCED = "SYNCED"
        const val STATUS_CONFLICT = "CONFLICT"

        const val ACTION_CREATE = "CREATE"
        const val ACTION_UPDATE = "UPDATE"
        const val ACTION_DELETE = "DELETE"

        const val ENTITY_ACTIVITY = "activity"
        const val ENTITY_CYCLE = "cycle"
        const val ENTITY_MEDICATION = "medication"
        const val ENTITY_PARTNER = "partner"
        const val ENTITY_HEALTH_REPORT = "health_report"
    }
}