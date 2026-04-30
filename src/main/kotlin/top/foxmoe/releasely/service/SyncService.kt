package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.SyncRecord
import top.foxmoe.releasely.mapper.SyncRecordMapper
import java.time.LocalDateTime

@Service
class SyncService(
    private val syncRecordMapper: SyncRecordMapper,
    private val objectMapper: ObjectMapper
) {

    fun getSyncStatus(userId: Long): SyncStatusResponse {
        val pendingCount = syncRecordMapper.selectCount(QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("sync_status", SyncRecord.STATUS_PENDING))

        val conflictCount = syncRecordMapper.selectCount(QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("sync_status", SyncRecord.STATUS_CONFLICT))

        val lastSync = syncRecordMapper.selectOne(QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("sync_status", SyncRecord.STATUS_SYNCED)
            .orderByDesc("server_timestamp")
            .last("LIMIT 1"))

        return SyncStatusResponse(
            pendingCount = pendingCount.toInt(),
            conflictCount = conflictCount.toInt(),
            lastSyncTime = lastSync?.serverTimestamp
        )
    }

    fun getPendingRecords(userId: Long): List<SyncRecord> {
        return syncRecordMapper.selectList(QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("sync_status", SyncRecord.STATUS_PENDING)
            .orderByAsc("local_timestamp"))
    }

    fun getPendingRecordDtos(userId: Long): List<SyncRecordDto> {
        return getPendingRecords(userId).map { it.toDto() }
    }

    fun getConflictRecords(userId: Long): List<SyncRecord> {
        return syncRecordMapper.selectList(QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("sync_status", SyncRecord.STATUS_CONFLICT)
            .orderByAsc("local_timestamp"))
    }

    fun getConflictRecordDtos(userId: Long): List<SyncRecordDto> {
        return getConflictRecords(userId).map { it.toDto() }
    }

    fun createSyncRecord(record: SyncRecord): Long {
        record.createdAt = LocalDateTime.now()
        record.syncStatus = SyncRecord.STATUS_PENDING
        syncRecordMapper.insert(record)
        return record.id ?: 0L
    }

    fun markAsSynced(recordId: Long) {
        val record = syncRecordMapper.selectById(recordId)
        if (record != null) {
            record.syncStatus = SyncRecord.STATUS_SYNCED
            record.serverTimestamp = LocalDateTime.now()
            syncRecordMapper.updateById(record)
        }
    }

    fun markAsConflict(recordId: Long, serverData: String) {
        val record = syncRecordMapper.selectById(recordId)
        if (record != null) {
            record.syncStatus = SyncRecord.STATUS_CONFLICT
            record.payload = serverData
            syncRecordMapper.updateById(record)
        }
    }

    fun resolveConflict(userId: Long, request: ResolveConflictRequest): Boolean {
        val wrapper = QueryWrapper<SyncRecord>()
            .eq("user_id", userId)
            .eq("entity_type", request.entityType)
            .eq("entity_id", request.entityId)
            .eq("sync_status", SyncRecord.STATUS_CONFLICT)

        val conflictRecord = syncRecordMapper.selectOne(wrapper)
            ?: return false

        conflictRecord.syncStatus = SyncRecord.STATUS_SYNCED
        conflictRecord.serverTimestamp = LocalDateTime.now()

        if (request.resolution == "MERGED" && request.mergedData != null) {
            conflictRecord.payload = request.mergedData
        }

        return syncRecordMapper.updateById(conflictRecord) > 0
    }

    fun sync(userId: Long, request: SyncRequest): SyncResponse {
        val serverTimestamp = LocalDateTime.now()
        val conflicts = mutableListOf<SyncConflictDto>()

        request.pendingRecords?.forEach { recordDto ->
            val record = SyncRecord(
                userId = userId,
                entityType = recordDto.entityType,
                entityId = recordDto.entityId,
                action = recordDto.action,
                localTimestamp = recordDto.localTimestamp,
                syncStatus = SyncRecord.STATUS_PENDING,
                payload = recordDto.payload
            )
            createSyncRecord(record)
        }

        val allPending = getPendingRecords(userId)
        val allConflicts = getConflictRecords(userId)

        allConflicts.forEach { conflict ->
            conflicts.add(SyncConflictDto(
                entityType = conflict.entityType ?: "",
                entityId = conflict.entityId ?: 0,
                localData = conflict.payload ?: "",
                serverData = conflict.payload ?: "",
                conflictTimestamp = conflict.localTimestamp ?: LocalDateTime.now()
            ))
        }

        return SyncResponse(
            syncedRecords = emptyList(),
            pendingRecords = allPending.map { it.toDto() },
            conflicts = conflicts,
            serverTimestamp = serverTimestamp
        )
    }

    private fun SyncRecord.toDto() = SyncRecordDto(
        id = id,
        userId = userId,
        entityType = entityType,
        entityId = entityId,
        action = action,
        localTimestamp = localTimestamp,
        serverTimestamp = serverTimestamp,
        syncStatus = syncStatus,
        payload = payload,
        createdAt = createdAt
    )
}