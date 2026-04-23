package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class SyncRecordDto(
    val id: Long? = null,
    val userId: Long? = null,
    val entityType: String? = null,
    val entityId: Long? = null,
    val action: String? = null,
    val localTimestamp: LocalDateTime? = null,
    val serverTimestamp: LocalDateTime? = null,
    val syncStatus: String? = null,
    val payload: String? = null,
    val createdAt: LocalDateTime? = null
)

data class SyncRequest(
    val userId: Long,
    val lastSyncTimestamp: LocalDateTime? = null,
    val pendingRecords: List<SyncRecordDto>? = null
)

data class SyncResponse(
    val syncedRecords: List<SyncRecordDto>,
    val pendingRecords: List<SyncRecordDto>,
    val conflicts: List<SyncConflictDto>,
    val serverTimestamp: LocalDateTime
)

data class SyncConflictDto(
    val entityType: String,
    val entityId: Long,
    val localData: String,
    val serverData: String,
    val conflictTimestamp: LocalDateTime
)

data class ResolveConflictRequest(
    val entityType: String,
    val entityId: Long,
    val resolution: String, // LOCAL, SERVER, or MERGED
    val mergedData: String? = null
)

data class SyncStatusResponse(
    val pendingCount: Int,
    val conflictCount: Int,
    val lastSyncTime: LocalDateTime?
)