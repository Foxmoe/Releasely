package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class ActivityDto(
    val id: Long? = null,
    val userId: Long? = null,
    val type: String? = null,
    val protection: String? = null,
    val pleasureRating: Int? = null,
    val healthStatus: String? = null,
    val occurredAt: LocalDateTime? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null
)

data class CreateActivityRequest(
    val userId: Long,
    val type: String,
    val protection: String? = null,
    val pleasureRating: Int? = null,
    val healthStatus: String? = null,
    val occurredAt: LocalDateTime? = null,
    val notes: String? = null
)

data class UpdateActivityRequest(
    val id: Long,
    val type: String? = null,
    val protection: String? = null,
    val pleasureRating: Int? = null,
    val healthStatus: String? = null,
    val occurredAt: LocalDateTime? = null,
    val notes: String? = null
)

data class ActivityListResponse(
    val activities: List<ActivityDto>,
    val total: Int
)